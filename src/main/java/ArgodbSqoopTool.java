import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.cli.RelatedOptions;
import com.cloudera.sqoop.cli.ToolOptions;
import com.cloudera.sqoop.manager.ImportJobContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.sqoop.tool.ImportTool;
import org.apache.sqoop.util.ImportException;

import java.io.IOException;


public class ArgodbSqoopTool extends ImportTool {
    public static String DBLINK_URL = "dblink-url";
    public static String DBLINK_USER = "dblink-user";
    public static String DBLINK_PASSWORD = "dblink-password";
    public static String HOLO_TABLE = "holotable";
    public static String LARGE_STRING_ENABLED = "large-string-enabled";
    public static String LARGE_STRING_MAX_SIZE = "large-string-max-size";
    public static String TMP_DIR = "tmp-dir";
    public static String COMPRESSION_TYPE = "compression-type";
    public static String ARGO_SOURCE_COLNAMES = "argo-source-colnames";

    private final ArgoDBCodeGenTool codeGenerator = new ArgoDBCodeGenTool();

    @Override
    public int run(SqoopOptions sqoopOptions) {
        try {
            this.init(sqoopOptions);
            codeGenerator.setManager(this.manager);
            String tableName = sqoopOptions.getTableName();
            String jarFile = codeGenerator.generateORM(sqoopOptions, tableName);
            Path outputPath = new Path(sqoopOptions.getTargetDir());
            ImportJobContext context = new ImportJobContext(tableName, jarFile, sqoopOptions, outputPath);
            context.setConnManager(this.manager);
            ImportHoloJobBase importer = new ImportHoloJobBase(sqoopOptions, context);
            Configuration conf = sqoopOptions.getConf();
//            conf.set(RECORD_DELIM,  "" + sqoopOptions.getOutputRecordDelim());
            importer.runImport(tableName, jarFile, sqoopOptions.getSplitByCol(), conf);
        } catch (IOException | ImportException e) {
            throw new RuntimeException(e);
        } finally {
            this.destroy(sqoopOptions);
        }
        return 0;
    }

    @Override
    public void configureOptions(ToolOptions toolOptions) {
        toolOptions.addUniqueOptions(this.getCommonOptions());
        toolOptions.addUniqueOptions(this.getImportOptions());
        toolOptions.addUniqueOptions(this.getOutputFormatOptions());
        toolOptions.addUniqueOptions(this.getInputFormatOptions());
        RelatedOptions formatOpts = new RelatedOptions("import holo");
        formatOpts.addOption(OptionBuilder.withArgName("url")
                .hasArg()
                .withLongOpt(DBLINK_URL)
                .create());
        formatOpts.addOption(OptionBuilder.withArgName("user")
                .hasArg()
                .withLongOpt(DBLINK_USER)
                .create());
        formatOpts.addOption(OptionBuilder.withArgName("password")
                .hasArg()
                .withLongOpt(DBLINK_PASSWORD)
                .create());
        formatOpts.addOption(OptionBuilder.withArgName("table-name")
                .hasArg()
                .withLongOpt(HOLO_TABLE)
                .create());
        formatOpts.addOption(OptionBuilder.withArgName("tmp-dir")
                .hasArg()
                .withLongOpt(TMP_DIR)
                .create());
        formatOpts.addOption(
          OptionBuilder.withArgName(LARGE_STRING_ENABLED).hasArg().withLongOpt(LARGE_STRING_ENABLED).create()
        );
        formatOpts.addOption(
          OptionBuilder.withArgName(LARGE_STRING_MAX_SIZE).hasArg().withLongOpt(LARGE_STRING_MAX_SIZE).create()
        );
        formatOpts.addOption(
          OptionBuilder.withArgName(COMPRESSION_TYPE).hasArg().withLongOpt(COMPRESSION_TYPE).create()
        );
        toolOptions.addUniqueOptions(formatOpts);
    }

    @Override
    public void applyOptions(CommandLine in, SqoopOptions out) throws SqoopOptions.InvalidOptionsException {
        try {
            super.applyOptions(in, out);
            Configuration newConf = out.getConf();
            if (in.hasOption(DBLINK_URL)) {
                newConf.set(DBLINK_URL, in.getOptionValue(DBLINK_URL));
            }
            if (in.hasOption(DBLINK_USER)) {
                newConf.set(DBLINK_USER, in.getOptionValue(DBLINK_USER));
            }
            if (in.hasOption(DBLINK_PASSWORD)) {
                newConf.set(DBLINK_PASSWORD, in.getOptionValue(DBLINK_PASSWORD));
            }
            if (in.hasOption(HOLO_TABLE)) {
                newConf.set(HOLO_TABLE, in.getOptionValue(HOLO_TABLE));
            }
            if (in.hasOption(TMP_DIR)) {
                newConf.set(TMP_DIR, in.getOptionValue(TMP_DIR));
            }
            if (in.hasOption(LARGE_STRING_ENABLED)) {
                newConf.set(LARGE_STRING_ENABLED, in.getOptionValue(LARGE_STRING_ENABLED));
            }
            if (in.hasOption(LARGE_STRING_MAX_SIZE)) {
                newConf.set(LARGE_STRING_MAX_SIZE, in.getOptionValue(LARGE_STRING_MAX_SIZE));
            }
            if (in.hasOption(COMPRESSION_TYPE)) {
                newConf.set(COMPRESSION_TYPE, in.getOptionValue(COMPRESSION_TYPE));
            }
            out.setConf(newConf);
        } catch (NumberFormatException nfe) {
            throw new SqoopOptions.InvalidOptionsException("Error: expected numeric argument.\n Try --help for usage.");
        }
    }

    @Override
    public void validateOptions(SqoopOptions options) throws SqoopOptions.InvalidOptionsException {
        this.validateImportOptions(options);
        this.validateCommonOptions(options);
    }

}
