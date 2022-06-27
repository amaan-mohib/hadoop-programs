import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class CountryTransactionCount {
    public static class Map extends MapReduceBase
            implements Mapper<LongWritable, Text, Text, IntWritable> {

        @Override
        public void map(LongWritable key, Text value,
                OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {
            // Getting a line from the CSV
            String line = value.toString();

            String[] data = line.split(",");

            // The column 1 is price
            // Converting the price from string to integer
            IntWritable price = new IntWritable(Integer.parseInt(data[1]));

            // Collecting the output
            output.collect(new Text(data[6]), price);

        }

    }

    public static class Reduce extends MapReduceBase
            implements Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterator<IntWritable> values,
                OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {
            int frequency = 0;
            while (values.hasNext()) {
                // Increasing the frequency count for a key
                frequency += values.next().get();
            }
            output.collect(key, new IntWritable(frequency));
        }

    }

    public static void main(String[] args) throws IOException {
        // Create a new Job & Set the Job Name
        JobConf conf = new JobConf(CountryTransactionCount.class);
        conf.setJobName("Awards");

        // Set the type of value we get at the output < Text , IntWritable >
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        // Set the Mapper class
        conf.setMapperClass(Map.class);

        // Set the Reducer and combiner class
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);

        // Set the Input and Output Format class
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        // Configure the input path and output path
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));
        JobClient.runJob(conf);
    }
}
