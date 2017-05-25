begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * An executable script, can't be used concurrently.  */
end_comment

begin_interface
DECL|interface|ExecutableScript
specifier|public
interface|interface
name|ExecutableScript
block|{
comment|/**      * Sets a runtime script parameter.      *<p>      * Note that this method may be slow, involving put() and get() calls      * to a hashmap or similar.      * @param name parameter name      * @param value parameter value      */
DECL|method|setNextVar
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**      * Executes the script.      */
DECL|method|run
name|Object
name|run
parameter_list|()
function_decl|;
DECL|interface|Compiled
interface|interface
name|Compiled
block|{
DECL|method|newInstance
name|ExecutableScript
name|newInstance
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
function_decl|;
block|}
DECL|field|CONTEXT
name|ScriptContext
argument_list|<
name|Compiled
argument_list|>
name|CONTEXT
init|=
operator|new
name|ScriptContext
argument_list|<>
argument_list|(
literal|"executable"
argument_list|,
name|Compiled
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// TODO: remove these once each has its own script interface
DECL|field|AGGS_CONTEXT
name|ScriptContext
argument_list|<
name|Compiled
argument_list|>
name|AGGS_CONTEXT
init|=
operator|new
name|ScriptContext
argument_list|<>
argument_list|(
literal|"aggs_executable"
argument_list|,
name|Compiled
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|UPDATE_CONTEXT
name|ScriptContext
argument_list|<
name|Compiled
argument_list|>
name|UPDATE_CONTEXT
init|=
operator|new
name|ScriptContext
argument_list|<>
argument_list|(
literal|"update"
argument_list|,
name|Compiled
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|INGEST_CONTEXT
name|ScriptContext
argument_list|<
name|Compiled
argument_list|>
name|INGEST_CONTEXT
init|=
operator|new
name|ScriptContext
argument_list|<>
argument_list|(
literal|"ingest"
argument_list|,
name|Compiled
operator|.
name|class
argument_list|)
decl_stmt|;
block|}
end_interface

end_unit

