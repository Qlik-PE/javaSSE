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

import qlik.sse.ServerSideExtension.FunctionDefinition;
import qlik.sse.ServerSideExtension.FunctionType;
import qlik.sse.ServerSideExtension.DataType;
import qlik.sse.ServerSideExtension.Parameter;


import java.util.List;

/**
 * An abstract base class that is used to create and manage SSE functions.
 *
 * All fields are set in the constructor.
 */
public abstract class PluginFunction {
    public static final boolean CACHE = true;
    public static final boolean NO_CACHE = false;

    private String name;
    private int functionId;
    private FunctionType functionType;
    private DataType returnType;
    private List<Parameter> parms;
    private FunctionDefinition functionDefinition;
    private boolean qlikCache;

    /**
     * The default constructor.
     */
    public PluginFunction() { }

    /**
     * Initializes the class.
     *
     * @param name the name of the function.
     * @param functionId the numeric id that identifies the function.
     * @param returnType the return type of the function (numeric, string)
     * @param parms an ordered List of parameters
     * @param qlikCache a boolean indicating whether the Qlik engine should cache the results of this computation.
     */
    public void init(String name, int functionId, DataType returnType, List<Parameter> parms, boolean qlikCache) {
        this.name = name;
        this.functionId = functionId;
        this.returnType = returnType;
        this.parms = parms;
        this.qlikCache = qlikCache;

        functionDefinition = FunctionDefinition.newBuilder()
                .setName(name)
                .setFunctionId(functionId)
                .setFunctionType(functionType)
                .setReturnType(returnType)
                .addAllParams(parms)
                .build();

    }

    /**
     * Set the function type of this function (scalar, tensor, aggregation)
     * @param functionType the FunctionType
     */
    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    /**
     * Get the FunctionDefinition of the function defined by this class.
     * @return an instance of FunctionDefinition.
     */
    public FunctionDefinition getFunctionDefinition() { return functionDefinition; }

    /**
     * Get the name of this function.
     * @return the name as a String.
     */
    public String getName() { return name; }

    /**
     * Get the function ID of this function.
     * @return the ID as an integer.
     */
    public int getFunctionId() { return functionId; }

    /**
     * get the function type (scalar, tensor, aggregation).
     * @return the FunctionType of this function.
     */
    public FunctionType getFunctionType() { return functionType; }

    /**
     * Get the return type of this function (numeric, string).
     * @return a DataType reflecting this setting.
     */
    public DataType getReturnType() { return returnType; }

    /**
     * Should results be cached?
     *
     * Return a boolean indicating whether the results of the
     * execution of this Function should be cached by the Qlik engine.
     * @return true if the results should be cached, false otherwise.
     */
    public boolean getQlikCache() { return qlikCache; }

    /**
     * Return a list of parameters that this function expects.
     * @return an ordered List of parameters.
     */
    public List<Parameter> getParms() { return parms; }

    /**
     * Is this a scalar function?
     * @return true if scalar, false otherwise.
     */
    public boolean isScalar() {
        return functionType == FunctionType.SCALAR;
    }

    /**
     * Is this a tensor function?
     * @return true if tensor, false otherwise.
     */
    public boolean isTensor() {
        return functionType == FunctionType.TENSOR;
    }

    /**
     * Is this an aggregation function?
     * @return true if aggregation, false otherwise.
     */
    public boolean isAggregation() {
        return functionType == FunctionType.AGGREGATION;
    }


}
