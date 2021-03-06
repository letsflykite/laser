package io.izenecloud.admm;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdmmIterationCombiner
		extends
		Reducer<NullWritable, AdmmReducerContextWritable, NullWritable, AdmmReducerContextWritable> {
	private static final Logger LOG = LoggerFactory
			.getLogger(AdmmIterationCombiner.class);
	private double[] zUpdated;
	private long count;
	private double rho;
	private double lambda;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		zUpdated = null;
		count = 0;
	}

	protected void reduce(NullWritable key,
			Iterable<AdmmReducerContextWritable> values, Context context)
			throws IOException, InterruptedException {

		for (AdmmReducerContextWritable reducerContextWritable : values) {
			AdmmReducerContext reducerContext = reducerContextWritable.get();
			if (null == this.zUpdated) {
				this.zUpdated = reducerContext.getZUpdated();
				this.rho = reducerContext.getRho();
				this.lambda = reducerContext.getLambdaValue();
				this.count = reducerContext.getCount();
			} else {
				double[] zUpdated = reducerContext.getZUpdated();
				for (int i = 0; i < zUpdated.length; i++) {
					this.zUpdated[i] += zUpdated[i];
				}
				this.count += reducerContext.getCount();
			}
		}
	}

	protected void cleanup(Context context) throws IOException,
			InterruptedException {
		AdmmReducerContext reducerContext = new AdmmReducerContext(null, null,
				null, zUpdated, 0.0, rho, lambda, count);
		LOG.info("Combine {} => 1", count);
		context.write(NullWritable.get(), new AdmmReducerContextWritable(
				reducerContext));
	}
}