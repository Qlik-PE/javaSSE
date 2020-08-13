/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qlik.sse.plugin;

import qlik.sse.ServerSideExtension.BundledRows;
import qlik.sse.ServerSideExtension.FunctionType;


/**
 * Abstract base class for a scalar function.
 */
public abstract class ScalarFunction extends PluginFunction {
    /**
     * The default constructor. It sets the function type to SCALAR.
     */
    public ScalarFunction() { super(); setFunctionType(FunctionType.SCALAR); }

    /**
     * Execute a scalar function.
     *
     * The logic that gets executed when this function is called.
     *
     * You must override this function in the derived class.
     *
     * This function is called from onNext() in the plugin.
     *
     * @param rows the rows that the function should execute against.
     *
     * @return an instance of BundledRows that represents the results of this execution.
     */
    public abstract BundledRows scalar(BundledRows rows);
}
