begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Analyzer
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
name|CharArraySet
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
name|LowerCaseFilter
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/** Simple regex-based analyzer based on PatternTokenizer + lowercase + stopwords */
end_comment

begin_class
DECL|class|PatternAnalyzer
specifier|public
specifier|final
class|class
name|PatternAnalyzer
extends|extends
name|Analyzer
block|{
DECL|field|pattern
specifier|private
specifier|final
name|Pattern
name|pattern
decl_stmt|;
DECL|field|lowercase
specifier|private
specifier|final
name|boolean
name|lowercase
decl_stmt|;
DECL|field|stopWords
specifier|private
specifier|final
name|CharArraySet
name|stopWords
decl_stmt|;
DECL|method|PatternAnalyzer
specifier|public
name|PatternAnalyzer
parameter_list|(
name|Pattern
name|pattern
parameter_list|,
name|boolean
name|lowercase
parameter_list|,
name|CharArraySet
name|stopWords
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|lowercase
operator|=
name|lowercase
expr_stmt|;
name|this
operator|.
name|stopWords
operator|=
name|stopWords
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createComponents
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|s
parameter_list|)
block|{
specifier|final
name|Tokenizer
name|tokenizer
init|=
operator|new
name|PatternTokenizer
argument_list|(
name|pattern
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|TokenStream
name|stream
init|=
name|tokenizer
decl_stmt|;
if|if
condition|(
name|lowercase
condition|)
block|{
name|stream
operator|=
operator|new
name|LowerCaseFilter
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stopWords
operator|!=
literal|null
condition|)
block|{
name|stream
operator|=
operator|new
name|StopFilter
argument_list|(
name|stream
argument_list|,
name|stopWords
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TokenStreamComponents
argument_list|(
name|tokenizer
argument_list|,
name|stream
argument_list|)
return|;
block|}
block|}
end_class

end_unit

