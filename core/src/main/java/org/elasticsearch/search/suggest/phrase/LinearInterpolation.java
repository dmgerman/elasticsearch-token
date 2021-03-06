begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.phrase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
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
name|index
operator|.
name|Terms
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
name|BytesRef
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
name|ParseField
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
name|ParsingException
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|XContentParser
operator|.
name|Token
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
name|suggest
operator|.
name|phrase
operator|.
name|WordScorer
operator|.
name|WordScorerFactory
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Linear interpolation smoothing model.  *<p>  * See<a  * href="http://en.wikipedia.org/wiki/N-gram#Smoothing_techniques">N-Gram  * Smoothing</a> for details.  *</p>  */
end_comment

begin_class
DECL|class|LinearInterpolation
specifier|public
specifier|final
class|class
name|LinearInterpolation
extends|extends
name|SmoothingModel
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"linear"
decl_stmt|;
DECL|field|PARSE_FIELD
specifier|static
specifier|final
name|ParseField
name|PARSE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
DECL|field|TRIGRAM_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|TRIGRAM_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"trigram_lambda"
argument_list|)
decl_stmt|;
DECL|field|BIGRAM_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|BIGRAM_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"bigram_lambda"
argument_list|)
decl_stmt|;
DECL|field|UNIGRAM_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|UNIGRAM_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"unigram_lambda"
argument_list|)
decl_stmt|;
DECL|field|trigramLambda
specifier|private
specifier|final
name|double
name|trigramLambda
decl_stmt|;
DECL|field|bigramLambda
specifier|private
specifier|final
name|double
name|bigramLambda
decl_stmt|;
DECL|field|unigramLambda
specifier|private
specifier|final
name|double
name|unigramLambda
decl_stmt|;
comment|/**      * Creates a linear interpolation smoothing model.      *      * Note: the lambdas must sum up to one.      *      * @param trigramLambda      *            the trigram lambda      * @param bigramLambda      *            the bigram lambda      * @param unigramLambda      *            the unigram lambda      */
DECL|method|LinearInterpolation
specifier|public
name|LinearInterpolation
parameter_list|(
name|double
name|trigramLambda
parameter_list|,
name|double
name|bigramLambda
parameter_list|,
name|double
name|unigramLambda
parameter_list|)
block|{
name|double
name|sum
init|=
name|trigramLambda
operator|+
name|bigramLambda
operator|+
name|unigramLambda
decl_stmt|;
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
name|sum
operator|-
literal|1.0
argument_list|)
operator|>
literal|0.001
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"linear smoothing lambdas must sum to 1"
argument_list|)
throw|;
block|}
name|this
operator|.
name|trigramLambda
operator|=
name|trigramLambda
expr_stmt|;
name|this
operator|.
name|bigramLambda
operator|=
name|bigramLambda
expr_stmt|;
name|this
operator|.
name|unigramLambda
operator|=
name|unigramLambda
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|LinearInterpolation
specifier|public
name|LinearInterpolation
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|trigramLambda
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|bigramLambda
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|unigramLambda
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|trigramLambda
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|bigramLambda
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|unigramLambda
argument_list|)
expr_stmt|;
block|}
DECL|method|getTrigramLambda
specifier|public
name|double
name|getTrigramLambda
parameter_list|()
block|{
return|return
name|this
operator|.
name|trigramLambda
return|;
block|}
DECL|method|getBigramLambda
specifier|public
name|double
name|getBigramLambda
parameter_list|()
block|{
return|return
name|this
operator|.
name|bigramLambda
return|;
block|}
DECL|method|getUnigramLambda
specifier|public
name|double
name|getUnigramLambda
parameter_list|()
block|{
return|return
name|this
operator|.
name|unigramLambda
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|XContentBuilder
name|innerToXContent
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
name|field
argument_list|(
name|TRIGRAM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|trigramLambda
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|BIGRAM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|bigramLambda
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|UNIGRAM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|unigramLambda
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|SmoothingModel
name|other
parameter_list|)
block|{
specifier|final
name|LinearInterpolation
name|otherModel
init|=
operator|(
name|LinearInterpolation
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|trigramLambda
argument_list|,
name|otherModel
operator|.
name|trigramLambda
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|bigramLambda
argument_list|,
name|otherModel
operator|.
name|bigramLambda
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|unigramLambda
argument_list|,
name|otherModel
operator|.
name|unigramLambda
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|trigramLambda
argument_list|,
name|bigramLambda
argument_list|,
name|unigramLambda
argument_list|)
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|LinearInterpolation
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|double
name|trigramLambda
init|=
literal|0.0
decl_stmt|;
name|double
name|bigramLambda
init|=
literal|0.0
decl_stmt|;
name|double
name|unigramLambda
init|=
literal|0.0
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|TRIGRAM_FIELD
operator|.
name|match
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|trigramLambda
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|trigramLambda
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"trigram_lambda must be positive"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|BIGRAM_FIELD
operator|.
name|match
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|bigramLambda
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|bigramLambda
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bigram_lambda must be positive"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|UNIGRAM_FIELD
operator|.
name|match
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|unigramLambda
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|unigramLambda
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unigram_lambda must be positive"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggester[phrase][smoothing][linear] doesn't support field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|NAME
operator|+
literal|"] unknown token ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|LinearInterpolation
argument_list|(
name|trigramLambda
argument_list|,
name|bigramLambda
argument_list|,
name|unigramLambda
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildWordScorerFactory
specifier|public
name|WordScorerFactory
name|buildWordScorerFactory
parameter_list|()
block|{
return|return
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Terms
name|terms
parameter_list|,
name|String
name|field
parameter_list|,
name|double
name|realWordLikelyhood
parameter_list|,
name|BytesRef
name|separator
parameter_list|)
lambda|->
operator|new
name|LinearInterpolatingScorer
argument_list|(
name|reader
argument_list|,
name|terms
argument_list|,
name|field
argument_list|,
name|realWordLikelyhood
argument_list|,
name|separator
argument_list|,
name|trigramLambda
argument_list|,
name|bigramLambda
argument_list|,
name|unigramLambda
argument_list|)
return|;
block|}
block|}
end_class

end_unit

