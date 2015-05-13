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
name|FieldMapper
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CommonTermsQueryParser
specifier|public
class|class
name|CommonTermsQueryParser
extends|extends
name|BaseQueryParserTemp
block|{
DECL|field|DEFAULT_MAX_TERM_DOC_FREQ
specifier|static
specifier|final
name|float
name|DEFAULT_MAX_TERM_DOC_FREQ
init|=
literal|0.01f
decl_stmt|;
DECL|field|DEFAULT_HIGH_FREQ_OCCUR
specifier|static
specifier|final
name|Occur
name|DEFAULT_HIGH_FREQ_OCCUR
init|=
name|Occur
operator|.
name|SHOULD
decl_stmt|;
DECL|field|DEFAULT_LOW_FREQ_OCCUR
specifier|static
specifier|final
name|Occur
name|DEFAULT_LOW_FREQ_OCCUR
init|=
name|Occur
operator|.
name|SHOULD
decl_stmt|;
DECL|field|DEFAULT_DISABLE_COORDS
specifier|static
specifier|final
name|boolean
name|DEFAULT_DISABLE_COORDS
init|=
literal|true
decl_stmt|;
annotation|@
name|Inject
DECL|method|CommonTermsQueryParser
specifier|public
name|CommonTermsQueryParser
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|CommonTermsQueryBuilder
operator|.
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
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
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query malformed, no field"
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
name|value
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|String
name|queryAnalyzer
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
name|disableCoords
init|=
name|DEFAULT_DISABLE_COORDS
decl_stmt|;
name|Occur
name|highFreqOccur
init|=
name|DEFAULT_HIGH_FREQ_OCCUR
decl_stmt|;
name|Occur
name|lowFreqOccur
init|=
name|DEFAULT_LOW_FREQ_OCCUR
decl_stmt|;
name|float
name|maxTermFrequency
init|=
name|DEFAULT_MAX_TERM_DOC_FREQ
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
literal|"minimum_should_match"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"minimumShouldMatch"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
literal|"low_freq"
operator|.
name|equals
argument_list|(
name|innerFieldName
argument_list|)
operator|||
literal|"lowFreq"
operator|.
name|equals
argument_list|(
name|innerFieldName
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
literal|"high_freq"
operator|.
name|equals
argument_list|(
name|innerFieldName
argument_list|)
operator|||
literal|"highFreq"
operator|.
name|equals
argument_list|(
name|innerFieldName
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
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query does not support ["
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
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query does not support ["
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
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|value
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
literal|"analyzer"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|analyzer
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] analyzer ["
operator|+
name|parser
operator|.
name|text
argument_list|()
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|queryAnalyzer
operator|=
name|analyzer
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"disable_coord"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"disableCoord"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|disableCoords
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
literal|"boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
literal|"high_freq_operator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"highFreqOperator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|op
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"or"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|highFreqOccur
operator|=
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"and"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|highFreqOccur
operator|=
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query requires operator to be either 'and' or 'or', not ["
operator|+
name|op
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"low_freq_operator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"lowFreqOperator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|op
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"or"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|lowFreqOccur
operator|=
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"and"
operator|.
name|equalsIgnoreCase
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|lowFreqOccur
operator|=
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query requires operator to be either 'and' or 'or', not ["
operator|+
name|op
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"minimum_should_match"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"minimumShouldMatch"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
literal|"cutoff_frequency"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|maxTermFrequency
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
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query does not support ["
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
name|value
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
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[common] query parsed in simplified form, with direct field name, but included more options than just the field name, possibly use its 'options' form, with 'query' element?"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"No text specified for text query"
argument_list|)
throw|;
block|}
name|String
name|field
decl_stmt|;
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|parseContext
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|field
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
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
name|analyzer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|queryAnalyzer
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|analyzer
operator|=
name|mapper
operator|.
name|searchAnalyzer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|==
literal|null
operator|&&
name|mapper
operator|!=
literal|null
condition|)
block|{
name|analyzer
operator|=
name|parseContext
operator|.
name|getSearchAnalyzer
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|analyzer
operator|=
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|queryAnalyzer
argument_list|)
expr_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No analyzer found for ["
operator|+
name|queryAnalyzer
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
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
name|maxTermFrequency
argument_list|,
name|disableCoords
argument_list|,
name|mapper
argument_list|)
decl_stmt|;
name|commonsQuery
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|parseQueryString
argument_list|(
name|commonsQuery
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|,
name|field
argument_list|,
name|parseContext
argument_list|,
name|analyzer
argument_list|,
name|lowFreqMinimumShouldMatch
argument_list|,
name|highFreqMinimumShouldMatch
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedQuery
argument_list|(
name|queryName
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
DECL|method|parseQueryString
specifier|private
specifier|final
name|Query
name|parseQueryString
parameter_list|(
name|ExtendedCommonTermsQuery
name|query
parameter_list|,
name|String
name|queryString
parameter_list|,
name|String
name|field
parameter_list|,
name|QueryParseContext
name|parseContext
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
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
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
block|}
end_class

end_unit

