begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.functionscore.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
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
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
comment|/**  * A query that uses a script to compute or influence the score of documents  * that match with the inner query or filter.  */
end_comment

begin_class
DECL|class|ScriptScoreFunctionBuilder
specifier|public
class|class
name|ScriptScoreFunctionBuilder
implements|implements
name|ScoreFunctionBuilder
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
init|=
literal|null
decl_stmt|;
DECL|method|ScriptScoreFunctionBuilder
specifier|public
name|ScriptScoreFunctionBuilder
parameter_list|()
block|{      }
DECL|method|script
specifier|public
name|ScriptScoreFunctionBuilder
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
comment|/**      * Sets the language of the script.      */
DECL|method|lang
specifier|public
name|ScriptScoreFunctionBuilder
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
comment|/**      * Additional parameters that can be provided to the script.      */
DECL|method|params
specifier|public
name|ScriptScoreFunctionBuilder
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
if|if
condition|(
name|this
operator|.
name|params
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|params
operator|.
name|putAll
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Additional parameters that can be provided to the script.      */
DECL|method|param
specifier|public
name|ScriptScoreFunctionBuilder
name|param
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|params
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
name|params
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
name|script
argument_list|)
expr_stmt|;
if|if
condition|(
name|lang
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"lang"
argument_list|,
name|lang
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|params
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"params"
argument_list|,
name|this
operator|.
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|ScriptScoreFunctionParser
operator|.
name|NAMES
index|[
literal|0
index|]
return|;
block|}
block|}
end_class

end_unit

