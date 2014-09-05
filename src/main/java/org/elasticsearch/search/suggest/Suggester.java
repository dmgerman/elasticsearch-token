begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
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
name|util
operator|.
name|CharsRefBuilder
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

begin_class
DECL|class|Suggester
specifier|public
specifier|abstract
class|class
name|Suggester
parameter_list|<
name|T
extends|extends
name|SuggestionSearchContext
operator|.
name|SuggestionContext
parameter_list|>
block|{
specifier|protected
specifier|abstract
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
argument_list|>
DECL|method|innerExecute
name|innerExecute
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|suggestion
parameter_list|,
name|IndexReader
name|indexReader
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|names
specifier|public
specifier|abstract
name|String
index|[]
name|names
parameter_list|()
function_decl|;
DECL|method|getContextParser
specifier|public
specifier|abstract
name|SuggestContextParser
name|getContextParser
parameter_list|()
function_decl|;
specifier|public
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
argument_list|>
DECL|method|execute
name|execute
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|suggestion
parameter_list|,
name|IndexReader
name|indexReader
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
comment|// #3469 We want to ignore empty shards
if|if
condition|(
name|indexReader
operator|.
name|numDocs
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|innerExecute
argument_list|(
name|name
argument_list|,
name|suggestion
argument_list|,
name|indexReader
argument_list|,
name|spare
argument_list|)
return|;
block|}
block|}
end_class

end_unit

