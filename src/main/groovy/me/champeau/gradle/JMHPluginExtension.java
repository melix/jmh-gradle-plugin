package me.champeau.gradle;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JMHPluginExtension {
    private final Project project;

    private String jmhVersion = "1.3.2";
    private boolean includeTests;

    private String include = ".*";
    private String exclude;
    private String benchmarkMode;
    private Integer iterations;
    private Integer batchSize;
    private Integer fork;
    private Boolean failOnError;
    private Boolean forceGC;
    private String jvm;
    private String jvmArgs;
    private String jvmArgsAppend;
    private String jvmArgsPrepend;
    private File humanOutputFile;
    private File resultsFile;
    private Integer operationsPerInvocation;
    private Map benchmarkParameters;
    private List<String> profilers;
    private String timeOnIteration;
    private String resultExtension;
    private String resultFormat;
    private Boolean synchronizeIterations;
    private Integer threads;
    private List<Integer> threadGroups;
    private String timeUnit;
    private String verbosity;
    private String warmup;
    private Integer warmupBatchSize;
    private Integer warmupForks;
    private Integer warmupIterations;
    private String warmupMode;
    private List<String> warmupBenchmarks;
    private boolean zip64 = false;

    public JMHPluginExtension(final Project project) {
        this.project = project;
    }

    public List<String> buildArgs() {
        resolveArgs();

        List<String> args = new ArrayList<String>();
        args.add(include);
        addOption(args, exclude, "e");
        addOption(args, iterations, "i");
        addOption(args, benchmarkMode, "bm");
        addOption(args, batchSize, "bs");
        addOption(args, fork, "f");
        addOption(args, failOnError, "foe");
        addOption(args, forceGC, "gc");
        addOption(args, jvm, "jvm");
        addOption(args, jvmArgs, "jvmArgs");
        addOption(args, jvmArgsAppend, "jvmArgsAppend");
        addOption(args, jvmArgsPrepend, "jvmArgsPrepend");
        addOption(args, humanOutputFile, "o");
        addOption(args, operationsPerInvocation, "opi");
        addOption(args, benchmarkParameters, "p");
        addOption(args, profilers, "prof");
        addOption(args, resultsFile, "rff");
        addOption(args, timeOnIteration, "r");
        addOption(args, resultFormat, "rf");
        addOption(args, synchronizeIterations, "si");
        addOption(args, threads, "t");
        addOption(args, threadGroups, "tg");
        addOption(args, timeUnit, "tu");
        addOption(args, verbosity, "v");
        addOption(args, warmup, "w");
        addOption(args, warmupBatchSize, "wbs");
        addOption(args, warmupForks, "wf");
        addOption(args, warmupIterations, "wi");
        addOption(args, warmupMode, "wm");
        addOption(args, warmupBenchmarks, "wmb");

        return args;
    }

    private void resolveArgs() {
        resolveResultExtension();
        resolveResultFormat();
        resolveResultsFile();
    }

    private void resolveResultsFile() {
        resultsFile = resultsFile != null ? resultsFile : project.file(String.valueOf(project.getBuildDir()) + "/reports/jmh/results." + resultExtension);
    }

    private void resolveResultExtension() {
        resultExtension = resultFormat != null ? parseResultFormat() : "txt";
    }

    private void resolveResultFormat() {
        resultFormat = resultFormat != null ? resultFormat : "text";
    }

    private String parseResultFormat() {
        return ResultFormatType.translate(resultFormat);
    }

    private void addOption(List<String> options, String str, String option) {
        if (str!=null) {
            options.add("-"+option);
            options.add(str);
        }
    }

    private void addOption(List<String> options, List values, String option) {
        if (values!=null) {
            options.add("-"+option);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                final Object value = values.get(i);
                sb.append(value);
                if (i<values.size()-1) {
                    sb.append(",");
                }
            }
            options.add(sb.toString());
        }
    }

    private void addOption(List<String> options, Boolean b, String option) {
        if (b!=null) {
            options.add("-"+option);
            options.add(b?"1":"0");
        }
    }

    private void addOption(List<String> options, Integer i, String option) {
        if (i!=null) {
            options.add("-"+option);
            options.add(String.valueOf(i));
        }
    }

    private void addOption(List<String> options, File f, String option) {
        if (f!=null) {
            options.add("-"+option);
            options.add(project.relativePath(f));
        }
    }

    @SuppressWarnings("unchecked")
    private void addOption(List<String> options, Map params, String option) {
        if (params!=null && !params.isEmpty()) {
            options.add("-"+option);
            StringBuilder sb = new StringBuilder();
            List<Map.Entry> values = new ArrayList<Map.Entry>(params.entrySet());
            for (int i = 0; i < values.size(); i++) {
                final Map.Entry entry = values.get(i);
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                if (i<values.size()-1) {
                    sb.append(",");
                }
            }
            options.add(sb.toString());
        }
    }
    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getBenchmarkMode() {
        return benchmarkMode;
    }

    public void setBenchmarkMode(String benchmarkMode) {
        this.benchmarkMode = benchmarkMode;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getFork() {
        return fork;
    }

    public void setFork(Integer fork) {
        this.fork = fork;
    }

    public Boolean getFailOnError() {
        return failOnError;
    }

    public void setFailOnError(Boolean failOnError) {
        this.failOnError = failOnError;
    }

    public Boolean getForceGC() {
        return forceGC;
    }

    public void setForceGC(Boolean forceGC) {
        this.forceGC = forceGC;
    }

    public String getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = jvm;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public String getJvmArgsAppend() {
        return jvmArgsAppend;
    }

    public void setJvmArgsAppend(String jvmArgsAppend) {
        this.jvmArgsAppend = jvmArgsAppend;
    }

    public String getJvmArgsPrepend() {
        return jvmArgsPrepend;
    }

    public void setJvmArgsPrepend(String jvmArgsPrepend) {
        this.jvmArgsPrepend = jvmArgsPrepend;
    }

    public File getHumanOutputFile() {
        return humanOutputFile;
    }

    public void setHumanOutputFile(File humanOutputFile) {
        this.humanOutputFile = humanOutputFile;
    }

    public File getResultsFile() {
        return resultsFile;
    }

    public void setResultsFile(File resultsFile) {
        this.resultsFile = resultsFile;
    }

    public Integer getOperationsPerInvocation() {
        return operationsPerInvocation;
    }

    public void setOperationsPerInvocation(Integer operationsPerInvocation) {
        this.operationsPerInvocation = operationsPerInvocation;
    }

    public Map getBenchmarkParameters() {
        return benchmarkParameters;
    }

    public void setBenchmarkParameters(Map benchmarkParameters) {
        this.benchmarkParameters = benchmarkParameters;
    }

    public List<String> getProfilers() {
        return profilers;
    }

    public void setProfilers(List<String> profilers) {
        this.profilers = profilers;
    }

    public String getTimeOnIteration() {
        return timeOnIteration;
    }

    public void setTimeOnIteration(String timeOnIteration) {
        this.timeOnIteration = timeOnIteration;
    }

    public String getResultFormat() {
        return resultFormat;
    }

    public void setResultFormat(String resultFormat) {
        this.resultFormat = resultFormat;
    }

    public Boolean getSynchronizeIterations() {
        return synchronizeIterations;
    }

    public void setSynchronizeIterations(Boolean synchronizeIterations) {
        this.synchronizeIterations = synchronizeIterations;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public List<Integer> getThreadGroups() {
        return threadGroups;
    }

    public void setThreadGroups(List<Integer> threadGroups) {
        this.threadGroups = threadGroups;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(String verbosity) {
        this.verbosity = verbosity;
    }

    public String getWarmup() {
        return warmup;
    }

    public void setWarmup(String warmup) {
        this.warmup = warmup;
    }

    public Integer getWarmupBatchSize() {
        return warmupBatchSize;
    }

    public void setWarmupBatchSize(Integer warmupBatchSize) {
        this.warmupBatchSize = warmupBatchSize;
    }

    public Integer getWarmupForks() {
        return warmupForks;
    }

    public void setWarmupForks(Integer warmupForks) {
        this.warmupForks = warmupForks;
    }

    public Integer getWarmupIterations() {
        return warmupIterations;
    }

    public void setWarmupIterations(Integer warmupIterations) {
        this.warmupIterations = warmupIterations;
    }

    public String getWarmupMode() {
        return warmupMode;
    }

    public void setWarmupMode(String warmupMode) {
        this.warmupMode = warmupMode;
    }

    public List<String> getWarmupBenchmarks() {
        return warmupBenchmarks;
    }

    public void setWarmupBenchmarks(List<String> warmupBenchmarks) {
        this.warmupBenchmarks = warmupBenchmarks;
    }

    public boolean isZip64() {
        return zip64;
    }

    public void setZip64(final boolean zip64) {
        this.zip64 = zip64;
    }

    public String getJmhVersion() {
        return jmhVersion;
    }

    public void setJmhVersion(String jmhVersion) {
        this.jmhVersion = jmhVersion;
    }

    public boolean isIncludeTests() {
        return includeTests;
    }

    public void setIncludeTests(boolean includeTests) {
        this.includeTests = includeTests;
    }

    private enum ResultFormatType {
        TEXT("txt"),
        CSV("cvs"),
        SCSV("scsv"),
        JSON("json"),
        LATEX("tex");

        private String extension;

        ResultFormatType(String extension) {
            this.extension = extension;
        }

        public static String translate(String resultFormat) {
            return ResultFormatType.valueOf(resultFormat.toUpperCase()).extension;
        }
    }
}
