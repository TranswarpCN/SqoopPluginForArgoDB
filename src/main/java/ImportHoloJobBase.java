import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.manager.ImportJobContext;
import com.cloudera.sqoop.mapreduce.DataDrivenImportJob;
import com.cloudera.sqoop.mapreduce.db.DataDrivenDBInputFormat;
import com.cloudera.sqoop.util.ImportException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import com.cloudera.sqoop.lib.SqoopRecord;

import java.io.IOException;

public class ImportHoloJobBase extends DataDrivenImportJob {

    private ImportJobContext context;
    public ImportHoloJobBase(SqoopOptions opts, ImportJobContext context) {
        super(opts, DataDrivenDBInputFormat.class, context);
        this.context = context;
    }

    @Override
    protected Class<? extends org.apache.hadoop.mapreduce.OutputFormat> getOutputFormatClass() throws ClassNotFoundException {
        return  HoloOutputFormat.class;
    }

    @Override
    protected ImportJobContext getContext() {
        return context;
    }

    @Override
    protected void jobTeardown(Job job) throws IOException, ImportException {
        super.jobTeardown(job);
        FileSystem fileSystem = FileSystem.get(job.getConfiguration());
        fileSystem.delete(this.getContext().getDestination(), true);
    }

    @Override
    protected Class<? extends Mapper> getMapperClass() {
        return ArgoImportMapper.class;
    }

    @Override
    protected void configureMapper(Job job, String tableName,
                                   String tableClassName) throws IOException {
        job.setOutputKeyClass(SqoopRecord.class);
        job.setOutputValueClass(NullWritable.class);

        job.setMapperClass(getMapperClass());
    }
}
