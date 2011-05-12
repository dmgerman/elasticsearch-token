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
name|Tokenizer
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
name|pattern
operator|.
name|PatternTokenizer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|common
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
name|common
operator|.
name|regex
operator|.
name|Regex
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
name|settings
operator|.
name|Settings
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
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_class
DECL|class|PatternTokenizerFactory
specifier|public
class|class
name|PatternTokenizerFactory
extends|extends
name|AbstractTokenizerFactory
block|{
DECL|field|pattern
specifier|private
specifier|final
name|Pattern
name|pattern
decl_stmt|;
DECL|field|group
specifier|private
specifier|final
name|int
name|group
decl_stmt|;
DECL|method|PatternTokenizerFactory
annotation|@
name|Inject
specifier|public
name|PatternTokenizerFactory
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
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|String
name|sPattern
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"pattern"
argument_list|,
literal|"\\W+"
comment|/*PatternAnalyzer.NON_WORD_PATTERN*/
argument_list|)
decl_stmt|;
if|if
condition|(
name|sPattern
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"pattern is missing for ["
operator|+
name|name
operator|+
literal|"] tokenizer of type 'pattern'"
argument_list|)
throw|;
block|}
name|this
operator|.
name|pattern
operator|=
name|Regex
operator|.
name|compile
argument_list|(
name|sPattern
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"flags"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|group
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"group"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|create
annotation|@
name|Override
specifier|public
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|PatternTokenizer
argument_list|(
name|reader
argument_list|,
name|pattern
argument_list|,
name|group
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"failed to create pattern tokenizer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

