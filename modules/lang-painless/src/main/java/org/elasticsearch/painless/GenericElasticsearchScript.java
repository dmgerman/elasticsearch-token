begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ScriptDocValues
import|;
end_import

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
comment|/**  * Generic script interface that Painless implements for all Elasticsearch scripts.  */
end_comment

begin_interface
DECL|interface|GenericElasticsearchScript
specifier|public
interface|interface
name|GenericElasticsearchScript
block|{
DECL|field|ARGUMENTS
name|String
index|[]
name|ARGUMENTS
init|=
operator|new
name|String
index|[]
block|{
literal|"params"
block|,
literal|"_score"
block|,
literal|"doc"
block|,
literal|"_value"
block|,
literal|"ctx"
block|}
decl_stmt|;
DECL|method|execute
name|Object
name|execute
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|,
name|double
name|_score
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptDocValues
argument_list|<
name|?
argument_list|>
argument_list|>
name|doc
parameter_list|,
name|Object
name|_value
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|ctx
parameter_list|)
function_decl|;
DECL|method|uses$_score
name|boolean
name|uses$_score
parameter_list|()
function_decl|;
DECL|method|uses$ctx
name|boolean
name|uses$ctx
parameter_list|()
function_decl|;
block|}
end_interface

end_unit
