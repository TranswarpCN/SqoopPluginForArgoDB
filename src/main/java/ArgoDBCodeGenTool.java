import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.orm.CompilationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.sqoop.tool.CodeGenTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by zhehaozhang on 22-12-6.
 */
public class ArgoDBCodeGenTool extends CodeGenTool {
  public static final Log LOG = LogFactory.getLog(CodeGenTool.class.getName());

  private List<String> argoGeneratedJarFiles;

  public ArgoDBCodeGenTool() {
    super();
    argoGeneratedJarFiles = new ArrayList<String>();
  }

  @Override
  public List<String> getGeneratedJarFiles() {
    ArrayList<String> out = new ArrayList<String>(argoGeneratedJarFiles);
    return out;
  }


  @Override
  public String generateORM(SqoopOptions options, String tableName)
      throws IOException {
    String existingJar = options.getExistingJarName();
    if (existingJar != null) {
      // This code generator is being invoked as part of an import or export
      // process, and the user has pre-specified a jar and class to use.
      // Don't generate.
      if (manager.isORMFacilitySelfManaged()) {
        // No need to generated any ORM.  Ignore any jar file given on
        // command line also.
        LOG.info("The connection manager declares that it self manages mapping"
            + " between records & fields and rows & columns.  The jar file "
            + " provided will have no effect");
      }
      LOG.info("Using existing jar: " + existingJar);
      return existingJar;
    }
    if (manager.isORMFacilitySelfManaged()) {
      // No need to generated any ORM.  Ignore any jar file given on
      // command line also.
      LOG.info("The connection manager declares that it self manages mapping"
          + " between records & fields and rows & columns.  No class will"
          + " will be generated.");
      return null;
    }
    LOG.info("Beginning code generation");
    CompilationManager compileMgr = new CompilationManager(options);
    ArgoDBClassWriter classWriter = new ArgoDBClassWriter(options, manager, tableName,
        compileMgr);
    classWriter.generate();
    options.getConf().set(ArgodbSqoopTool.ARGO_SOURCE_COLNAMES, StringUtils.join(classWriter.getColNames(), ","));
    compileMgr.compile();
    compileMgr.jar();
    String jarFile = compileMgr.getJarFilename();
    this.argoGeneratedJarFiles.add(jarFile);
    return jarFile;
  }

}
