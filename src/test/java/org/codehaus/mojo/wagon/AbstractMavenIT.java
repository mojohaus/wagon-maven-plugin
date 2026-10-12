package org.codehaus.mojo.wagon;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.executor.ExecutorHelper;
import org.apache.maven.executor.ExecutorRequest;
import org.apache.maven.executor.ExecutorResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs the test projects in {@code src/test/projects} with the Maven installation that runs this build, forked, against
 * the local repository into which maven-invoker-plugin installed the plugin under test.
 */
abstract class AbstractMavenIT {

    private static final Path PROJECTS = Paths.get("src", "test", "projects");

    private static final Duration TIMEOUT = Duration.ofMinutes(10);

    /**
     * @param name a directory in {@code src/test/projects}
     * @return the source directory of that test project
     */
    protected static Path projectSource(String name) {
        return PROJECTS.resolve(name);
    }

    /**
     * Copies a test project to {@code target/it-projects/<test class>/<name>}, replacing an earlier copy.
     *
     * @param name a directory in {@code src/test/projects}
     * @return the copy, where the build runs
     * @throws IOException if the copy fails
     */
    protected Path prepareProject(String name) throws IOException {
        Path source = projectSource(name);
        Path target = Paths.get("target", "it-projects", getClass().getSimpleName(), name);
        if (Files.exists(target)) {
            try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList())) {
                    Files.delete(path);
                }
            }
        }
        Files.createDirectories(target.getParent());
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.collect(Collectors.toList())) {
                Files.copy(path, target.resolve(source.relativize(path).toString()));
            }
        }
        return target.toAbsolutePath();
    }

    /**
     * Builds a test project and fails unless the build succeeds without {@code [ERROR]} lines. The output goes to
     * {@code build.log} in the project, as it is written, so it survives a timeout.
     *
     * @param projectDir the project to build, from {@link #prepareProject(String)}
     * @param arguments Maven command line options and goals
     * @throws Exception if Maven cannot be run
     */
    protected void executeErrorFree(Path projectDir, String... arguments) throws Exception {
        String mavenHome = System.getProperty("maven.home");
        if (mavenHome == null || mavenHome.isEmpty()) {
            fail("The 'maven.home' system property must name the Maven installation that runs the integration tests"
                    + " (see the maven-failsafe-plugin configuration in pom.xml)");
        }

        List<String> args = new ArrayList<>();
        args.add("-B");
        args.add("-Dmaven.repo.local=" + System.getProperty("it.localRepository"));
        args.add("-Dit-plugin.version=" + System.getProperty("it.pluginVersion"));
        args.addAll(Arrays.asList(arguments));

        Path log = projectDir.resolve("build.log");
        ExecutorResult result;
        // forked: the build must not share this JVM's system properties and logging with the test
        try (OutputStream out = Files.newOutputStream(log);
                ExecutorHelper maven =
                        ExecutorHelper.forMavenInstallation(Paths.get(mavenHome), ExecutorHelper.Mode.FORKED)) {
            ExecutorRequest request = ExecutorRequest.mavenBuilder()
                    .cwd(projectDir)
                    .arguments(args)
                    .stdOut(out)
                    .stdErr(out)
                    .executionTimeout(TIMEOUT)
                    .build();
            result = maven.execute(request);
        }

        List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        List<String> errors =
                lines.stream().filter(line -> line.startsWith("[ERROR]")).collect(Collectors.toList());
        assertTrue(
                result.success() && errors.isEmpty(),
                () -> "Build of " + projectDir + " failed (exit code "
                        + result.exitCode().map(String::valueOf).orElse("?") + "), see " + log + ":\n"
                        + String.join("\n", errors.isEmpty() ? lines : errors));
    }
}
