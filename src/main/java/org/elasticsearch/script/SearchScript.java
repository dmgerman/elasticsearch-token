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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|ReaderContextAware
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|ScorerAware
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
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
comment|/**  * A search script.  *  * @see ExplainableSearchScript for script which can explain a score  */
end_comment

begin_interface
DECL|interface|SearchScript
specifier|public
interface|interface
name|SearchScript
extends|extends
name|ExecutableScript
extends|,
name|ReaderContextAware
extends|,
name|ScorerAware
block|{
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
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|script
specifier|private
name|String
name|script
decl_stmt|;
DECL|field|lang
specifier|private
name|String
name|lang
decl_stmt|;
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|method|script
specifier|public
name|Builder
name|script
parameter_list|(
name|String
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lang
specifier|public
name|Builder
name|lang
parameter_list|(
name|String
name|lang
parameter_list|)
block|{
name|this
operator|.
name|lang
operator|=
name|lang
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|params
specifier|public
name|Builder
name|params
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|SearchScript
name|build
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
name|build
argument_list|(
name|context
operator|.
name|scriptService
argument_list|()
argument_list|,
name|context
operator|.
name|lookup
argument_list|()
argument_list|)
return|;
block|}
DECL|method|build
specifier|public
name|SearchScript
name|build
parameter_list|(
name|ScriptService
name|service
parameter_list|,
name|SearchLookup
name|lookup
parameter_list|)
block|{
return|return
name|service
operator|.
name|search
argument_list|(
name|lookup
argument_list|,
name|lang
argument_list|,
name|script
argument_list|,
name|params
argument_list|)
return|;
block|}
block|}
block|}
end_interface

end_unit

