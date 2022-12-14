package tde2.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.BasicConfigurator;
import tde2.SetupHelper;
import tde2.Transaction;
import tde2.customwritable.CompositeKeyWritable;

import java.io.IOException;

// Objetivo: Obter a relação de número de transações a cada ano e direção de fluxo.
public class Job7
{
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
    {
        BasicConfigurator.configure();
        Job job = Job.getInstance(new Configuration(), "job7");

        if (!SetupHelper.setupIO(job, args))
            return;

        job.setJarByClass(Job7.class);
        SetupHelper.setupMapper(job, Map.class, CompositeKeyWritable.class, IntWritable.class);
        SetupHelper.setupReducer(job, Reduce.class, CompositeKeyWritable.class, IntWritable.class);

        job.waitForCompletion(true);
    }

    public static class Map extends Mapper<LongWritable, Text, CompositeKeyWritable, IntWritable>
    {
        public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException
        {
            Transaction t = new Transaction(value.toString());

            if (key.get() == 0 && t.isHeader() || !t.isValid())
                return;

            con.write(new CompositeKeyWritable(t.getFlow(), String.valueOf(t.getYear())), new IntWritable(1));
        }
    }

    public static class Reduce extends Reducer<CompositeKeyWritable, IntWritable, CompositeKeyWritable, IntWritable>
    {
        public void reduce(CompositeKeyWritable key, Iterable<IntWritable> values, Context con) throws IOException, InterruptedException
        {
            int total = 0;

            for (IntWritable value : values)
                total += value.get();


            con.write(key, new IntWritable(total));
        }
    }
}