import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import com.cloudera.sqoop.lib.LargeObjectLoader;
import com.cloudera.sqoop.lib.SqoopRecord;
import com.cloudera.sqoop.mapreduce.AutoProgressMapper;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by zhehaozhang on 22-12-6.
 */
public class ArgoImportMapper extends AutoProgressMapper<LongWritable, SqoopRecord, SqoopRecord, NullWritable> {

  private LargeObjectLoader lobLoader;

  @Override
  protected void setup(Context context)
      throws IOException, InterruptedException {
    this.lobLoader = new LargeObjectLoader(context.getConfiguration(),
        FileOutputFormat.getWorkOutputPath(context));
  }

  @Override
  public void map(LongWritable key, SqoopRecord val, Context context)
      throws IOException, InterruptedException {

    try {
      // Loading of LOBs was delayed until we have a Context.
      val.loadLargeObjects(lobLoader);
    } catch (SQLException sqlE) {
      throw new IOException(sqlE);
    }

    context.write(val, NullWritable.get());
  }

  @Override
  protected void cleanup(Context context) throws IOException {
    if (null != lobLoader) {
      lobLoader.close();
    }
  }
}
