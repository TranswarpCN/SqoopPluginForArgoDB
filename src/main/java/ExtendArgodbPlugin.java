import com.cloudera.sqoop.tool.ToolDesc;
import org.apache.sqoop.tool.ToolPlugin;

import java.util.Collections;
import java.util.List;

public class ExtendArgodbPlugin extends ToolPlugin {

  @Override
  public List<ToolDesc> getTools() {
    return Collections.singletonList(new ToolDesc("argodbSqoopTool", ArgodbSqoopTool.class, "外部数据源的数据导入holodesk"));
  }
}
