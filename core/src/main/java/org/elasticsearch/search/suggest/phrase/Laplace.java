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
name|index
operator|.
name|query
operator|.
name|QueryParseContext
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
comment|/**  * An<a href="http://en.wikipedia.org/wiki/Additive_smoothing">additive  * smoothing</a> model.  *<p>  * See<a  * href="http://en.wikipedia.org/wiki/N-gram#Smoothing_techniques">N-Gram  * Smoothing</a> for details.  *</p>  */
end_comment

begin_class
DECL|class|Laplace
specifier|public
specifier|final
class|class
name|Laplace
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
literal|"laplace"
decl_stmt|;
DECL|field|ALPHA_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|ALPHA_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"alpha"
argument_list|)
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
comment|/**      * Default alpha parameter for laplace smoothing      */
DECL|field|DEFAULT_LAPLACE_ALPHA
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_LAPLACE_ALPHA
init|=
literal|0.5
decl_stmt|;
DECL|field|alpha
specifier|private
name|double
name|alpha
init|=
name|DEFAULT_LAPLACE_ALPHA
decl_stmt|;
comment|/**      * Creates a Laplace smoothing model.      *      */
DECL|method|Laplace
specifier|public
name|Laplace
parameter_list|(
name|double
name|alpha
parameter_list|)
block|{
name|this
operator|.
name|alpha
operator|=
name|alpha
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|Laplace
specifier|public
name|Laplace
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|alpha
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
name|alpha
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return the laplace model alpha parameter      */
DECL|method|getAlpha
specifier|public
name|double
name|getAlpha
parameter_list|()
block|{
return|return
name|this
operator|.
name|alpha
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
name|ALPHA_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|alpha
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
name|Laplace
name|otherModel
init|=
operator|(
name|Laplace
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|alpha
argument_list|,
name|otherModel
operator|.
name|alpha
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
specifier|final
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|alpha
argument_list|)
return|;
block|}
DECL|method|innerFromXContent
specifier|public
specifier|static
name|SmoothingModel
name|innerFromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
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
name|alpha
init|=
name|DEFAULT_LAPLACE_ALPHA
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
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
operator|&&
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|fieldName
argument_list|,
name|ALPHA_FIELD
argument_list|)
condition|)
block|{
name|alpha
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Laplace
argument_list|(
name|alpha
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
name|LaplaceScorer
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
name|alpha
argument_list|)
return|;
block|}
block|}
end_class

end_unit
