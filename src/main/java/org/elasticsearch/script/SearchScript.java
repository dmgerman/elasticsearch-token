begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
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
comment|/**  * A search script.  *   * @see ExplanableSearchScript for script which can explain a score  */
end_comment

begin_interface
DECL|interface|SearchScript
specifier|public
interface|interface
name|SearchScript
extends|extends
name|ExecutableScript
block|{
DECL|method|setScorer
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
function_decl|;
DECL|method|setNextReader
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
function_decl|;
DECL|method|setNextDocId
name|void
name|setNextDocId
parameter_list|(
name|int
name|doc
parameter_list|)
function_decl|;
DECL|method|setNextSource
name|void
name|setNextSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
function_decl|;
DECL|method|setNextScore
name|void
name|setNextScore
parameter_list|(
name|float
name|score
parameter_list|)
function_decl|;
DECL|method|runAsFloat
name|float
name|runAsFloat
parameter_list|()
function_decl|;
DECL|method|runAsLong
name|long
name|runAsLong
parameter_list|()
function_decl|;
DECL|method|runAsDouble
name|double
name|runAsDouble
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

