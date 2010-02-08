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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|CustomAnalyzer
specifier|public
class|class
name|CustomAnalyzer
extends|extends
name|Analyzer
implements|implements
name|PositionIncrementGapAnalyzer
block|{
DECL|field|tokenizerFactory
specifier|private
specifier|final
name|TokenizerFactory
name|tokenizerFactory
decl_stmt|;
DECL|field|tokenFilters
specifier|private
specifier|final
name|TokenFilterFactory
index|[]
name|tokenFilters
decl_stmt|;
DECL|field|positionIncrementGap
specifier|private
name|int
name|positionIncrementGap
init|=
literal|0
decl_stmt|;
DECL|method|CustomAnalyzer
specifier|public
name|CustomAnalyzer
parameter_list|(
name|TokenizerFactory
name|tokenizerFactory
parameter_list|,
name|TokenFilterFactory
index|[]
name|tokenFilters
parameter_list|)
block|{
name|this
operator|.
name|tokenizerFactory
operator|=
name|tokenizerFactory
expr_stmt|;
name|this
operator|.
name|tokenFilters
operator|=
name|tokenFilters
expr_stmt|;
block|}
DECL|method|setPositionIncrementGap
annotation|@
name|Override
specifier|public
name|void
name|setPositionIncrementGap
parameter_list|(
name|int
name|positionIncrementGap
parameter_list|)
block|{
name|this
operator|.
name|positionIncrementGap
operator|=
name|positionIncrementGap
expr_stmt|;
block|}
DECL|method|tokenizerFactory
specifier|public
name|TokenizerFactory
name|tokenizerFactory
parameter_list|()
block|{
return|return
name|tokenizerFactory
return|;
block|}
DECL|method|tokenFilters
specifier|public
name|TokenFilterFactory
index|[]
name|tokenFilters
parameter_list|()
block|{
return|return
name|tokenFilters
return|;
block|}
DECL|method|getPositionIncrementGap
annotation|@
name|Override
specifier|public
name|int
name|getPositionIncrementGap
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|this
operator|.
name|positionIncrementGap
return|;
block|}
DECL|method|tokenStream
annotation|@
name|Override
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Reader
name|reader
parameter_list|)
block|{
return|return
name|buildHolder
argument_list|(
name|reader
argument_list|)
operator|.
name|tokenStream
return|;
block|}
DECL|method|reusableTokenStream
annotation|@
name|Override
specifier|public
name|TokenStream
name|reusableTokenStream
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|Holder
name|holder
init|=
operator|(
name|Holder
operator|)
name|getPreviousTokenStream
argument_list|()
decl_stmt|;
if|if
condition|(
name|holder
operator|==
literal|null
condition|)
block|{
name|holder
operator|=
name|buildHolder
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|setPreviousTokenStream
argument_list|(
name|holder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|holder
operator|.
name|tokenizer
operator|.
name|reset
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
return|return
name|holder
operator|.
name|tokenStream
return|;
block|}
DECL|method|buildHolder
specifier|private
name|Holder
name|buildHolder
parameter_list|(
name|Reader
name|input
parameter_list|)
block|{
name|Tokenizer
name|tokenizer
init|=
name|tokenizerFactory
operator|.
name|create
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|TokenStream
name|tokenStream
init|=
name|tokenizer
decl_stmt|;
for|for
control|(
name|TokenFilterFactory
name|tokenFilter
range|:
name|tokenFilters
control|)
block|{
name|tokenStream
operator|=
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenStream
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Holder
argument_list|(
name|tokenizer
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
DECL|class|Holder
specifier|private
specifier|static
class|class
name|Holder
block|{
DECL|field|tokenizer
specifier|final
name|Tokenizer
name|tokenizer
decl_stmt|;
DECL|field|tokenStream
specifier|final
name|TokenStream
name|tokenStream
decl_stmt|;
DECL|method|Holder
specifier|private
name|Holder
parameter_list|(
name|Tokenizer
name|tokenizer
parameter_list|,
name|TokenStream
name|tokenStream
parameter_list|)
block|{
name|this
operator|.
name|tokenizer
operator|=
name|tokenizer
expr_stmt|;
name|this
operator|.
name|tokenStream
operator|=
name|tokenStream
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

