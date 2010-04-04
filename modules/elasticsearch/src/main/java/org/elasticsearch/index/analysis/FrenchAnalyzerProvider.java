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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|Iterators
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|fr
operator|.
name|FrenchAnalyzer
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
name|Version
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FrenchAnalyzerProvider
specifier|public
class|class
name|FrenchAnalyzerProvider
extends|extends
name|AbstractAnalyzerProvider
argument_list|<
name|FrenchAnalyzer
argument_list|>
block|{
DECL|field|stopWords
specifier|private
specifier|final
name|Set
argument_list|<
name|?
argument_list|>
name|stopWords
decl_stmt|;
DECL|field|stemExclusion
specifier|private
specifier|final
name|Set
argument_list|<
name|?
argument_list|>
name|stemExclusion
decl_stmt|;
DECL|field|analyzer
specifier|private
specifier|final
name|FrenchAnalyzer
name|analyzer
decl_stmt|;
DECL|method|FrenchAnalyzerProvider
annotation|@
name|Inject
specifier|public
name|FrenchAnalyzerProvider
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
name|FrenchAnalyzer
operator|.
name|getDefaultStopSet
argument_list|()
expr_stmt|;
block|}
name|String
index|[]
name|stemExclusion
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"stem_exclusion"
argument_list|)
decl_stmt|;
if|if
condition|(
name|stemExclusion
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|stemExclusion
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|Iterators
operator|.
name|forArray
argument_list|(
name|stemExclusion
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|stemExclusion
operator|=
name|ImmutableSet
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
name|analyzer
operator|=
operator|new
name|FrenchAnalyzer
argument_list|(
name|Version
operator|.
name|LUCENE_CURRENT
argument_list|,
name|this
operator|.
name|stopWords
argument_list|,
name|this
operator|.
name|stemExclusion
argument_list|)
expr_stmt|;
block|}
DECL|method|get
annotation|@
name|Override
specifier|public
name|FrenchAnalyzer
name|get
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
return|;
block|}
block|}
end_class

end_unit

