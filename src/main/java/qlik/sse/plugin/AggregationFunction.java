
package qlik.sse.plugin;

import qlik.sse.generated.ServerSideExtension.FunctionType;
import qlik.sse.generated.ServerSideExtension.BundledRows;

/**
 * An abstract base class for an aggregation function.
 *
 * Note that aggregations must potentially accumulate across multiple
 * calls to this function if the total number of rows is large, so a
 * "reduce" function is also required for an aggregation. It will be called
 * after all data has been processed.
 */
public abstract class AggregationFunction extends PluginFunction {

    /**
     * The default constructor. It sets the function type to AGGREGATION.
     */
    public AggregationFunction() { super(); setFunctionType(FunctionType.AGGREGATION); }

    /**
     * Execute an aggregation function.
     *
     * The logic that gets executed when this function is called.
     *
     * You must override this function in the derived class.
     *
     * This function is called from onNext() in the plugin.
     *
     * @param rows the rows that the function should execute against.
     *
     */
    public abstract void aggregation(BundledRows rows);

    /**
     * Aggregation complete. Combine/reduce results and return.
     *
     * This function is called to do the final aggregation of the
     * result from each iteration of the call to aggregation. It is called
     * from onCompleted() in the plugin.
     *
     * Note that you should be careful to clear/reset/reallocate any storage
     * used to accumulate state during the aggregation process.
     *
     * @return the aggregation result as BundledRows.
     */
    public abstract BundledRows reduce();
}
