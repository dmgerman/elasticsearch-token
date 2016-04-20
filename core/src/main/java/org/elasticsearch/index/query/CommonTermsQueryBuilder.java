begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|Term
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
name|queries
operator|.
name|ExtendedCommonTermsQuery
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
name|search
operator|.
name|BooleanClause
operator|.
name|Occur
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
name|search
operator|.
name|BooleanQuery
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
name|search
operator|.
name|Query
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
name|search
operator|.
name|similarities
operator|.
name|Similarity
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
name|BytesRefBuilder
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
name|Strings
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
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_comment
comment|/**  * CommonTermsQuery query is a query that executes high-frequency terms in a  * optional sub-query to prevent slow queries due to "common" terms like  * stopwords. This query basically builds 2 queries off the {@code #add(Term)  * added} terms where low-frequency terms are added to a required boolean clause  * and high-frequency terms are added to an optional boolean clause. The  * optional clause is only executed if the required "low-frequency' clause  * matches. Scores produced by this query will be slightly different to plain  * {@link BooleanQuery} scorer mainly due to differences in the  * {@link Similarity#coord(int,int) number of leave queries} in the required  * boolean clause. In the most cases high-frequency terms are unlikely to  * significantly contribute to the document score unless at least one of the  * low-frequency terms are matched such that this query can improve query  * execution times significantly if applicable.  */
end_comment

begin_class
DECL|class|CommonTermsQueryBuilder
specifier|public
class|class
name|CommonTermsQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|CommonTermsQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"common"
decl_stmt|;
DECL|field|QUERY_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_CUTOFF_FREQ
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_CUTOFF_FREQ
init|=
literal|0.01f
decl_stmt|;
DECL|field|DEFAULT_HIGH_FREQ_OCCUR
specifier|public
specifier|static
specifier|final
name|Operator
name|DEFAULT_HIGH_FREQ_OCCUR
init|=
name|Operator
operator|.
name|OR
decl_stmt|;
DECL|field|DEFAULT_LOW_FREQ_OCCUR
specifier|public
specifier|static
specifier|final
name|Operator
name|DEFAULT_LOW_FREQ_OCCUR
init|=
name|Operator
operator|.
name|OR
decl_stmt|;
DECL|field|DEFAULT_DISABLE_COORD
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_DISABLE_COORD
init|=
literal|true
decl_stmt|;
DECL|field|CUTOFF_FREQUENCY_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|CUTOFF_FREQUENCY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"cutoff_frequency"
argument_list|)
decl_stmt|;
DECL|field|MINIMUM_SHOULD_MATCH_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|MINIMUM_SHOULD_MATCH_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"minimum_should_match"
argument_list|)
decl_stmt|;
DECL|field|LOW_FREQ_OPERATOR_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|LOW_FREQ_OPERATOR_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"low_freq_operator"
argument_list|)
decl_stmt|;
DECL|field|HIGH_FREQ_OPERATOR_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|HIGH_FREQ_OPERATOR_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"high_freq_operator"
argument_list|)
decl_stmt|;
DECL|field|DISABLE_COORD_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|DISABLE_COORD_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"disable_coord"
argument_list|)
decl_stmt|;
DECL|field|ANALYZER_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|ANALYZER_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"analyzer"
argument_list|)
decl_stmt|;
DECL|field|QUERY_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|QUERY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"query"
argument_list|)
decl_stmt|;
DECL|field|HIGH_FREQ_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|HIGH_FREQ_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"high_freq"
argument_list|)
decl_stmt|;
DECL|field|LOW_FREQ_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|LOW_FREQ_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"low_freq"
argument_list|)
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|text
specifier|private
specifier|final
name|Object
name|text
decl_stmt|;
DECL|field|highFreqOperator
specifier|private
name|Operator
name|highFreqOperator
init|=
name|DEFAULT_HIGH_FREQ_OCCUR
decl_stmt|;
DECL|field|lowFreqOperator
specifier|private
name|Operator
name|lowFreqOperator
init|=
name|DEFAULT_LOW_FREQ_OCCUR
decl_stmt|;
DECL|field|analyzer
specifier|private
name|String
name|analyzer
init|=
literal|null
decl_stmt|;
DECL|field|lowFreqMinimumShouldMatch
specifier|private
name|String
name|lowFreqMinimumShouldMatch
init|=
literal|null
decl_stmt|;
DECL|field|highFreqMinimumShouldMatch
specifier|private
name|String
name|highFreqMinimumShouldMatch
init|=
literal|null
decl_stmt|;
DECL|field|disableCoord
specifier|private
name|boolean
name|disableCoord
init|=
name|DEFAULT_DISABLE_COORD
decl_stmt|;
DECL|field|cutoffFrequency
specifier|private
name|float
name|cutoffFrequency
init|=
name|DEFAULT_CUTOFF_FREQ
decl_stmt|;
comment|/**      * Constructs a new common terms query.      */
DECL|method|CommonTermsQueryBuilder
specifier|public
name|CommonTermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|text
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field name is null or empty"
argument_list|)
throw|;
block|}
if|if
condition|(
name|text
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"text cannot be null."
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|text
operator|=
name|text
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|CommonTermsQueryBuilder
specifier|public
name|CommonTermsQueryBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|text
operator|=
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
name|highFreqOperator
operator|=
name|Operator
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|lowFreqOperator
operator|=
name|Operator
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|analyzer
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|lowFreqMinimumShouldMatch
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|highFreqMinimumShouldMatch
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|disableCoord
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|cutoffFrequency
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|this
operator|.
name|text
argument_list|)
expr_stmt|;
name|highFreqOperator
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|lowFreqOperator
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|lowFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|highFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|disableCoord
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|cutoffFrequency
argument_list|)
expr_stmt|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
DECL|method|value
specifier|public
name|Object
name|value
parameter_list|()
block|{
return|return
name|this
operator|.
name|text
return|;
block|}
comment|/**      * Sets the operator to use for terms with a high document frequency      * (greater than or equal to {@link #cutoffFrequency(float)}. Defaults to      *<tt>AND</tt>.      */
DECL|method|highFreqOperator
specifier|public
name|CommonTermsQueryBuilder
name|highFreqOperator
parameter_list|(
name|Operator
name|operator
parameter_list|)
block|{
name|this
operator|.
name|highFreqOperator
operator|=
operator|(
name|operator
operator|==
literal|null
operator|)
condition|?
name|DEFAULT_HIGH_FREQ_OCCUR
else|:
name|operator
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highFreqOperator
specifier|public
name|Operator
name|highFreqOperator
parameter_list|()
block|{
return|return
name|highFreqOperator
return|;
block|}
comment|/**      * Sets the operator to use for terms with a low document frequency (less      * than {@link #cutoffFrequency(float)}. Defaults to<tt>AND</tt>.      */
DECL|method|lowFreqOperator
specifier|public
name|CommonTermsQueryBuilder
name|lowFreqOperator
parameter_list|(
name|Operator
name|operator
parameter_list|)
block|{
name|this
operator|.
name|lowFreqOperator
operator|=
operator|(
name|operator
operator|==
literal|null
operator|)
condition|?
name|DEFAULT_LOW_FREQ_OCCUR
else|:
name|operator
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lowFreqOperator
specifier|public
name|Operator
name|lowFreqOperator
parameter_list|()
block|{
return|return
name|lowFreqOperator
return|;
block|}
comment|/**      * Explicitly set the analyzer to use. Defaults to use explicit mapping      * config for the field, or, if not set, the default search analyzer.      */
DECL|method|analyzer
specifier|public
name|CommonTermsQueryBuilder
name|analyzer
parameter_list|(
name|String
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|analyzer
specifier|public
name|String
name|analyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
return|;
block|}
comment|/**      * Sets the cutoff document frequency for high / low frequent terms. A value      * in [0..1] (or absolute number&gt;=1) representing the maximum threshold of      * a terms document frequency to be considered a low frequency term.      * Defaults to      *<tt>{@value #DEFAULT_CUTOFF_FREQ}</tt>      */
DECL|method|cutoffFrequency
specifier|public
name|CommonTermsQueryBuilder
name|cutoffFrequency
parameter_list|(
name|float
name|cutoffFrequency
parameter_list|)
block|{
name|this
operator|.
name|cutoffFrequency
operator|=
name|cutoffFrequency
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|cutoffFrequency
specifier|public
name|float
name|cutoffFrequency
parameter_list|()
block|{
return|return
name|this
operator|.
name|cutoffFrequency
return|;
block|}
comment|/**      * Sets the minimum number of high frequent query terms that need to match in order to      * produce a hit when there are no low frequent terms.      */
DECL|method|highFreqMinimumShouldMatch
specifier|public
name|CommonTermsQueryBuilder
name|highFreqMinimumShouldMatch
parameter_list|(
name|String
name|highFreqMinimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|highFreqMinimumShouldMatch
operator|=
name|highFreqMinimumShouldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highFreqMinimumShouldMatch
specifier|public
name|String
name|highFreqMinimumShouldMatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|highFreqMinimumShouldMatch
return|;
block|}
comment|/**      * Sets the minimum number of low frequent query terms that need to match in order to      * produce a hit.      */
DECL|method|lowFreqMinimumShouldMatch
specifier|public
name|CommonTermsQueryBuilder
name|lowFreqMinimumShouldMatch
parameter_list|(
name|String
name|lowFreqMinimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|lowFreqMinimumShouldMatch
operator|=
name|lowFreqMinimumShouldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lowFreqMinimumShouldMatch
specifier|public
name|String
name|lowFreqMinimumShouldMatch
parameter_list|()
block|{
return|return
name|this
operator|.
name|lowFreqMinimumShouldMatch
return|;
block|}
DECL|method|disableCoord
specifier|public
name|CommonTermsQueryBuilder
name|disableCoord
parameter_list|(
name|boolean
name|disableCoord
parameter_list|)
block|{
name|this
operator|.
name|disableCoord
operator|=
name|disableCoord
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|disableCoord
specifier|public
name|boolean
name|disableCoord
parameter_list|()
block|{
return|return
name|this
operator|.
name|disableCoord
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
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
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|QUERY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|text
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|DISABLE_COORD_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|disableCoord
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|HIGH_FREQ_OPERATOR_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|highFreqOperator
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|LOW_FREQ_OPERATOR_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|lowFreqOperator
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|ANALYZER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|CUTOFF_FREQUENCY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|cutoffFrequency
argument_list|)
expr_stmt|;
if|if
condition|(
name|lowFreqMinimumShouldMatch
operator|!=
literal|null
operator|||
name|highFreqMinimumShouldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|MINIMUM_SHOULD_MATCH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lowFreqMinimumShouldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|LOW_FREQ_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|lowFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|highFreqMinimumShouldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|HIGH_FREQ_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|highFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|Optional
argument_list|<
name|CommonTermsQueryBuilder
argument_list|>
name|fromXContent
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
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
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
literal|"] query malformed, no field"
argument_list|)
throw|;
block|}
name|String
name|fieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|Object
name|text
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|analyzer
init|=
literal|null
decl_stmt|;
name|String
name|lowFreqMinimumShouldMatch
init|=
literal|null
decl_stmt|;
name|String
name|highFreqMinimumShouldMatch
init|=
literal|null
decl_stmt|;
name|boolean
name|disableCoord
init|=
name|CommonTermsQueryBuilder
operator|.
name|DEFAULT_DISABLE_COORD
decl_stmt|;
name|Operator
name|highFreqOperator
init|=
name|CommonTermsQueryBuilder
operator|.
name|DEFAULT_HIGH_FREQ_OCCUR
decl_stmt|;
name|Operator
name|lowFreqOperator
init|=
name|CommonTermsQueryBuilder
operator|.
name|DEFAULT_LOW_FREQ_OCCUR
decl_stmt|;
name|float
name|cutoffFrequency
init|=
name|CommonTermsQueryBuilder
operator|.
name|DEFAULT_CUTOFF_FREQ
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|currentFieldName
init|=
literal|null
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
name|XContentParser
operator|.
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
name|currentFieldName
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MINIMUM_SHOULD_MATCH_FIELD
argument_list|)
condition|)
block|{
name|String
name|innerFieldName
init|=
literal|null
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
name|XContentParser
operator|.
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
name|innerFieldName
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
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|innerFieldName
argument_list|,
name|LOW_FREQ_FIELD
argument_list|)
condition|)
block|{
name|lowFreqMinimumShouldMatch
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|innerFieldName
argument_list|,
name|HIGH_FREQ_FIELD
argument_list|)
condition|)
block|{
name|highFreqMinimumShouldMatch
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
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
name|CommonTermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|innerFieldName
operator|+
literal|"] for ["
operator|+
name|currentFieldName
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
name|CommonTermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] unexpected token type ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|innerFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
name|CommonTermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
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
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|QUERY_FIELD
argument_list|)
condition|)
block|{
name|text
operator|=
name|parser
operator|.
name|objectText
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ANALYZER_FIELD
argument_list|)
condition|)
block|{
name|analyzer
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|DISABLE_COORD_FIELD
argument_list|)
condition|)
block|{
name|disableCoord
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|HIGH_FREQ_OPERATOR_FIELD
argument_list|)
condition|)
block|{
name|highFreqOperator
operator|=
name|Operator
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|LOW_FREQ_OPERATOR_FIELD
argument_list|)
condition|)
block|{
name|lowFreqOperator
operator|=
name|Operator
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MINIMUM_SHOULD_MATCH_FIELD
argument_list|)
condition|)
block|{
name|lowFreqMinimumShouldMatch
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|CUTOFF_FREQUENCY_FIELD
argument_list|)
condition|)
block|{
name|cutoffFrequency
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
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
name|CommonTermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|text
operator|=
name|parser
operator|.
name|objectText
argument_list|()
expr_stmt|;
comment|// move to the next token
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
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
literal|"[common] query parsed in simplified form, with direct field name, but included more options than just "
operator|+
literal|"the field name, possibly use its 'options' form, with 'query' element?"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|text
operator|==
literal|null
condition|)
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
literal|"No text specified for text query"
argument_list|)
throw|;
block|}
return|return
name|Optional
operator|.
name|of
argument_list|(
operator|new
name|CommonTermsQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|text
argument_list|)
operator|.
name|lowFreqMinimumShouldMatch
argument_list|(
name|lowFreqMinimumShouldMatch
argument_list|)
operator|.
name|highFreqMinimumShouldMatch
argument_list|(
name|highFreqMinimumShouldMatch
argument_list|)
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
operator|.
name|highFreqOperator
argument_list|(
name|highFreqOperator
argument_list|)
operator|.
name|lowFreqOperator
argument_list|(
name|lowFreqOperator
argument_list|)
operator|.
name|disableCoord
argument_list|(
name|disableCoord
argument_list|)
operator|.
name|cutoffFrequency
argument_list|(
name|cutoffFrequency
argument_list|)
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
argument_list|)
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
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|field
decl_stmt|;
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|field
operator|=
name|fieldType
operator|.
name|name
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|field
operator|=
name|fieldName
expr_stmt|;
block|}
name|Analyzer
name|analyzerObj
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|analyzerObj
operator|=
name|context
operator|.
name|getSearchAnalyzer
argument_list|(
name|fieldType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|analyzerObj
operator|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|analyzerObj
operator|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
if|if
condition|(
name|analyzerObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"[common] analyzer ["
operator|+
name|analyzer
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
block|}
name|Occur
name|highFreqOccur
init|=
name|highFreqOperator
operator|.
name|toBooleanClauseOccur
argument_list|()
decl_stmt|;
name|Occur
name|lowFreqOccur
init|=
name|lowFreqOperator
operator|.
name|toBooleanClauseOccur
argument_list|()
decl_stmt|;
name|ExtendedCommonTermsQuery
name|commonsQuery
init|=
operator|new
name|ExtendedCommonTermsQuery
argument_list|(
name|highFreqOccur
argument_list|,
name|lowFreqOccur
argument_list|,
name|cutoffFrequency
argument_list|,
name|disableCoord
argument_list|,
name|fieldType
argument_list|)
decl_stmt|;
return|return
name|parseQueryString
argument_list|(
name|commonsQuery
argument_list|,
name|text
argument_list|,
name|field
argument_list|,
name|analyzerObj
argument_list|,
name|lowFreqMinimumShouldMatch
argument_list|,
name|highFreqMinimumShouldMatch
argument_list|)
return|;
block|}
DECL|method|parseQueryString
specifier|private
specifier|static
name|Query
name|parseQueryString
parameter_list|(
name|ExtendedCommonTermsQuery
name|query
parameter_list|,
name|Object
name|queryString
parameter_list|,
name|String
name|field
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|String
name|lowFreqMinimumShouldMatch
parameter_list|,
name|String
name|highFreqMinimumShouldMatch
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Logic similar to QueryParser#getFieldQuery
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
init|(
name|TokenStream
name|source
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|field
argument_list|,
name|queryString
operator|.
name|toString
argument_list|()
argument_list|)
init|)
block|{
name|source
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|termAtt
init|=
name|source
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|BytesRefBuilder
name|builder
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|source
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
comment|// UTF-8
name|builder
operator|.
name|copyChars
argument_list|(
name|termAtt
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|,
name|builder
operator|.
name|toBytesRef
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|query
operator|.
name|setLowFreqMinimumNumberShouldMatch
argument_list|(
name|lowFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
name|query
operator|.
name|setHighFreqMinimumNumberShouldMatch
argument_list|(
name|highFreqMinimumShouldMatch
argument_list|)
expr_stmt|;
return|return
name|query
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
name|fieldName
argument_list|,
name|text
argument_list|,
name|highFreqOperator
argument_list|,
name|lowFreqOperator
argument_list|,
name|analyzer
argument_list|,
name|lowFreqMinimumShouldMatch
argument_list|,
name|highFreqMinimumShouldMatch
argument_list|,
name|disableCoord
argument_list|,
name|cutoffFrequency
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|CommonTermsQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldName
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|text
argument_list|,
name|other
operator|.
name|text
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|highFreqOperator
argument_list|,
name|other
operator|.
name|highFreqOperator
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|lowFreqOperator
argument_list|,
name|other
operator|.
name|lowFreqOperator
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|analyzer
argument_list|,
name|other
operator|.
name|analyzer
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|lowFreqMinimumShouldMatch
argument_list|,
name|other
operator|.
name|lowFreqMinimumShouldMatch
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|highFreqMinimumShouldMatch
argument_list|,
name|other
operator|.
name|highFreqMinimumShouldMatch
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|disableCoord
argument_list|,
name|other
operator|.
name|disableCoord
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|cutoffFrequency
argument_list|,
name|other
operator|.
name|cutoffFrequency
argument_list|)
return|;
block|}
block|}
end_class

end_unit

