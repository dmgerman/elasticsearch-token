begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|analysis
operator|.
name|StopAnalyzer
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
name|analysis
operator|.
name|StopFilter
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
name|analysis
operator|.
name|TokenStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Iterators
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|StopTokenFilterFactory
specifier|public
class|class
name|StopTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|stopWords
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|stopWords
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|private
specifier|final
name|boolean
name|enablePositionIncrements
decl_stmt|;
DECL|field|ignoreCase
specifier|private
specifier|final
name|boolean
name|ignoreCase
decl_stmt|;
DECL|method|StopTokenFilterFactory
annotation|@
name|Inject
specifier|public
name|StopTokenFilterFactory
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|String
index|[]
name|stopWords
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"stopwords"
argument_list|)
decl_stmt|;
if|if
condition|(
name|stopWords
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|stopWords
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|Iterators
operator|.
name|forArray
argument_list|(
name|stopWords
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|stopWords
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
operator|(
name|Iterable
argument_list|<
name|?
extends|extends
name|String
argument_list|>
operator|)
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|enablePositionIncrements
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"enable_position_increments"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|ignoreCase
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"ignore_case"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|create
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
return|return
operator|new
name|StopFilter
argument_list|(
name|enablePositionIncrements
argument_list|,
name|tokenStream
argument_list|,
name|stopWords
argument_list|,
name|ignoreCase
argument_list|)
return|;
block|}
DECL|method|stopWords
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|stopWords
parameter_list|()
block|{
return|return
name|stopWords
return|;
block|}
DECL|method|enablePositionIncrements
specifier|public
name|boolean
name|enablePositionIncrements
parameter_list|()
block|{
return|return
name|enablePositionIncrements
return|;
block|}
DECL|method|ignoreCase
specifier|public
name|boolean
name|ignoreCase
parameter_list|()
block|{
return|return
name|ignoreCase
return|;
block|}
block|}
end_class

end_unit

