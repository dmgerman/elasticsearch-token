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
name|queries
operator|.
name|TermsQuery
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
name|action
operator|.
name|termvectors
operator|.
name|MultiTermVectorsResponse
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
name|Nullable
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
name|lucene
operator|.
name|search
operator|.
name|MoreLikeThisQuery
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
name|analysis
operator|.
name|Analysis
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
operator|.
name|UidFieldMapper
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
name|MoreLikeThisQueryBuilder
operator|.
name|Item
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
name|search
operator|.
name|morelikethis
operator|.
name|MoreLikeThisFetchService
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
name|internal
operator|.
name|SearchContext
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|Uid
operator|.
name|createUidAsBytes
import|;
end_import

begin_comment
comment|/**  * Parser for the The More Like This Query (MLT Query) which finds documents that are "like" a given set of documents.  *  * The documents are provided as a set of strings and/or a list of {@link Item}.  */
end_comment

begin_class
DECL|class|MoreLikeThisQueryParser
specifier|public
class|class
name|MoreLikeThisQueryParser
implements|implements
name|QueryParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"mlt"
decl_stmt|;
DECL|field|fetchService
specifier|private
name|MoreLikeThisFetchService
name|fetchService
init|=
literal|null
decl_stmt|;
DECL|interface|Field
specifier|public
interface|interface
name|Field
block|{
DECL|field|FIELDS
name|ParseField
name|FIELDS
init|=
operator|new
name|ParseField
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
DECL|field|LIKE
name|ParseField
name|LIKE
init|=
operator|new
name|ParseField
argument_list|(
literal|"like"
argument_list|)
decl_stmt|;
DECL|field|UNLIKE
name|ParseField
name|UNLIKE
init|=
operator|new
name|ParseField
argument_list|(
literal|"unlike"
argument_list|)
decl_stmt|;
DECL|field|LIKE_TEXT
name|ParseField
name|LIKE_TEXT
init|=
operator|new
name|ParseField
argument_list|(
literal|"like_text"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"like"
argument_list|)
decl_stmt|;
DECL|field|IDS
name|ParseField
name|IDS
init|=
operator|new
name|ParseField
argument_list|(
literal|"ids"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"like"
argument_list|)
decl_stmt|;
DECL|field|DOCS
name|ParseField
name|DOCS
init|=
operator|new
name|ParseField
argument_list|(
literal|"docs"
argument_list|)
operator|.
name|withAllDeprecated
argument_list|(
literal|"like"
argument_list|)
decl_stmt|;
DECL|field|MAX_QUERY_TERMS
name|ParseField
name|MAX_QUERY_TERMS
init|=
operator|new
name|ParseField
argument_list|(
literal|"max_query_terms"
argument_list|)
decl_stmt|;
DECL|field|MIN_TERM_FREQ
name|ParseField
name|MIN_TERM_FREQ
init|=
operator|new
name|ParseField
argument_list|(
literal|"min_term_freq"
argument_list|)
decl_stmt|;
DECL|field|MIN_DOC_FREQ
name|ParseField
name|MIN_DOC_FREQ
init|=
operator|new
name|ParseField
argument_list|(
literal|"min_doc_freq"
argument_list|)
decl_stmt|;
DECL|field|MAX_DOC_FREQ
name|ParseField
name|MAX_DOC_FREQ
init|=
operator|new
name|ParseField
argument_list|(
literal|"max_doc_freq"
argument_list|)
decl_stmt|;
DECL|field|MIN_WORD_LENGTH
name|ParseField
name|MIN_WORD_LENGTH
init|=
operator|new
name|ParseField
argument_list|(
literal|"min_word_length"
argument_list|,
literal|"min_word_len"
argument_list|)
decl_stmt|;
DECL|field|MAX_WORD_LENGTH
name|ParseField
name|MAX_WORD_LENGTH
init|=
operator|new
name|ParseField
argument_list|(
literal|"max_word_length"
argument_list|,
literal|"max_word_len"
argument_list|)
decl_stmt|;
DECL|field|STOP_WORDS
name|ParseField
name|STOP_WORDS
init|=
operator|new
name|ParseField
argument_list|(
literal|"stop_words"
argument_list|)
decl_stmt|;
DECL|field|ANALYZER
name|ParseField
name|ANALYZER
init|=
operator|new
name|ParseField
argument_list|(
literal|"analyzer"
argument_list|)
decl_stmt|;
DECL|field|MINIMUM_SHOULD_MATCH
name|ParseField
name|MINIMUM_SHOULD_MATCH
init|=
operator|new
name|ParseField
argument_list|(
literal|"minimum_should_match"
argument_list|)
decl_stmt|;
DECL|field|BOOST_TERMS
name|ParseField
name|BOOST_TERMS
init|=
operator|new
name|ParseField
argument_list|(
literal|"boost_terms"
argument_list|)
decl_stmt|;
DECL|field|INCLUDE
name|ParseField
name|INCLUDE
init|=
operator|new
name|ParseField
argument_list|(
literal|"include"
argument_list|)
decl_stmt|;
DECL|field|FAIL_ON_UNSUPPORTED_FIELD
name|ParseField
name|FAIL_ON_UNSUPPORTED_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"fail_on_unsupported_field"
argument_list|)
decl_stmt|;
block|}
DECL|method|MoreLikeThisQueryParser
specifier|public
name|MoreLikeThisQueryParser
parameter_list|()
block|{      }
annotation|@
name|Inject
argument_list|(
name|optional
operator|=
literal|true
argument_list|)
DECL|method|setFetchService
specifier|public
name|void
name|setFetchService
parameter_list|(
annotation|@
name|Nullable
name|MoreLikeThisFetchService
name|fetchService
parameter_list|)
block|{
name|this
operator|.
name|fetchService
operator|=
name|fetchService
expr_stmt|;
block|}
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
name|NAME
block|,
literal|"more_like_this"
block|,
literal|"moreLikeThis"
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
name|ParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|MoreLikeThisQuery
name|mltQuery
init|=
operator|new
name|MoreLikeThisQuery
argument_list|()
decl_stmt|;
name|mltQuery
operator|.
name|setSimilarity
argument_list|(
name|parseContext
operator|.
name|searchSimilarity
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|likeTexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|unlikeTexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Item
argument_list|>
name|likeItems
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Item
argument_list|>
name|unlikeItems
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|moreLikeFields
init|=
literal|null
decl_stmt|;
name|Analyzer
name|analyzer
init|=
literal|null
decl_stmt|;
name|boolean
name|include
init|=
literal|false
decl_stmt|;
name|boolean
name|failOnUnsupportedField
init|=
literal|true
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|LIKE
argument_list|)
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|likeTexts
argument_list|,
name|likeItems
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|UNLIKE
argument_list|)
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|unlikeTexts
argument_list|,
name|unlikeItems
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|LIKE_TEXT
argument_list|)
condition|)
block|{
name|likeTexts
operator|.
name|add
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MAX_QUERY_TERMS
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMaxQueryTerms
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MIN_TERM_FREQ
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMinTermFrequency
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MIN_DOC_FREQ
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMinDocFreq
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MAX_DOC_FREQ
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMaxDocFreq
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MIN_WORD_LENGTH
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMinWordLen
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MAX_WORD_LENGTH
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMaxWordLen
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|ANALYZER
argument_list|)
condition|)
block|{
name|analyzer
operator|=
name|parseContext
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|MINIMUM_SHOULD_MATCH
argument_list|)
condition|)
block|{
name|mltQuery
operator|.
name|setMinimumShouldMatch
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|BOOST_TERMS
argument_list|)
condition|)
block|{
name|float
name|boostFactor
init|=
name|parser
operator|.
name|floatValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|boostFactor
operator|!=
literal|0
condition|)
block|{
name|mltQuery
operator|.
name|setBoostTerms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mltQuery
operator|.
name|setBoostTermsFactor
argument_list|(
name|boostFactor
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|INCLUDE
argument_list|)
condition|)
block|{
name|include
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|FAIL_ON_UNSUPPORTED_FIELD
argument_list|)
condition|)
block|{
name|failOnUnsupportedField
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
name|mltQuery
operator|.
name|setBoost
argument_list|(
name|parser
operator|.
name|floatValue
argument_list|()
argument_list|)
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
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[mlt] query does not support ["
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|FIELDS
argument_list|)
condition|)
block|{
name|moreLikeFields
operator|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
expr_stmt|;
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
name|END_ARRAY
condition|)
block|{
name|String
name|field
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|MappedFieldType
name|fieldType
init|=
name|parseContext
operator|.
name|fieldMapper
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|moreLikeFields
operator|.
name|add
argument_list|(
name|fieldType
operator|==
literal|null
condition|?
name|field
else|:
name|fieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|LIKE
argument_list|)
condition|)
block|{
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
name|END_ARRAY
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|likeTexts
argument_list|,
name|likeItems
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|UNLIKE
argument_list|)
condition|)
block|{
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
name|END_ARRAY
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|unlikeTexts
argument_list|,
name|unlikeItems
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|IDS
argument_list|)
condition|)
block|{
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
name|END_ARRAY
condition|)
block|{
if|if
condition|(
operator|!
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ids array element should only contain ids"
argument_list|)
throw|;
block|}
name|likeItems
operator|.
name|add
argument_list|(
operator|new
name|Item
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|parser
operator|.
name|text
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|DOCS
argument_list|)
condition|)
block|{
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
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"docs array element should include an object"
argument_list|)
throw|;
block|}
name|likeItems
operator|.
name|add
argument_list|(
name|Item
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|,
operator|new
name|Item
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|STOP_WORDS
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|stopWords
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|END_ARRAY
condition|)
block|{
name|stopWords
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|mltQuery
operator|.
name|setStopWords
argument_list|(
name|stopWords
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[mlt] query does not support ["
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|LIKE
argument_list|)
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|likeTexts
argument_list|,
name|likeItems
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|Field
operator|.
name|UNLIKE
argument_list|)
condition|)
block|{
name|parseLikeField
argument_list|(
name|parseContext
argument_list|,
name|unlikeTexts
argument_list|,
name|unlikeItems
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[mlt] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|likeTexts
operator|.
name|isEmpty
argument_list|()
operator|&&
name|likeItems
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"more_like_this requires 'like' to be specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|moreLikeFields
operator|!=
literal|null
operator|&&
name|moreLikeFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"more_like_this requires 'fields' to be non-empty"
argument_list|)
throw|;
block|}
comment|// set analyzer
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
name|mltQuery
operator|.
name|setAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
comment|// set like text fields
name|boolean
name|useDefaultField
init|=
operator|(
name|moreLikeFields
operator|==
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|useDefaultField
condition|)
block|{
name|moreLikeFields
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|parseContext
operator|.
name|defaultField
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// possibly remove unsupported fields
name|removeUnsupportedFields
argument_list|(
name|moreLikeFields
argument_list|,
name|analyzer
argument_list|,
name|failOnUnsupportedField
argument_list|)
expr_stmt|;
if|if
condition|(
name|moreLikeFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|mltQuery
operator|.
name|setMoreLikeFields
argument_list|(
name|moreLikeFields
operator|.
name|toArray
argument_list|(
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
comment|// support for named query
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
name|mltQuery
argument_list|)
expr_stmt|;
block|}
comment|// handle like texts
if|if
condition|(
operator|!
name|likeTexts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mltQuery
operator|.
name|setLikeText
argument_list|(
name|likeTexts
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|unlikeTexts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mltQuery
operator|.
name|setUnlikeText
argument_list|(
name|unlikeTexts
argument_list|)
expr_stmt|;
block|}
comment|// handle items
if|if
condition|(
operator|!
name|likeItems
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|handleItems
argument_list|(
name|parseContext
argument_list|,
name|mltQuery
argument_list|,
name|likeItems
argument_list|,
name|unlikeItems
argument_list|,
name|include
argument_list|,
name|moreLikeFields
argument_list|,
name|useDefaultField
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|mltQuery
return|;
block|}
block|}
DECL|method|parseLikeField
specifier|private
specifier|static
name|void
name|parseLikeField
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|texts
parameter_list|,
name|List
argument_list|<
name|Item
argument_list|>
name|items
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
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|texts
operator|.
name|add
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
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|items
operator|.
name|add
argument_list|(
name|Item
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|,
operator|new
name|Item
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Content of 'like' parameter should either be a string or an object"
argument_list|)
throw|;
block|}
block|}
DECL|method|removeUnsupportedFields
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|removeUnsupportedFields
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|moreLikeFields
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|boolean
name|failOnUnsupportedField
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|it
init|=
name|moreLikeFields
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Analysis
operator|.
name|generatesCharacterTokenStream
argument_list|(
name|analyzer
argument_list|,
name|fieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|failOnUnsupportedField
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"more_like_this doesn't support binary/numeric fields: ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|moreLikeFields
return|;
block|}
DECL|method|handleItems
specifier|private
name|Query
name|handleItems
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|MoreLikeThisQuery
name|mltQuery
parameter_list|,
name|List
argument_list|<
name|Item
argument_list|>
name|likeItems
parameter_list|,
name|List
argument_list|<
name|Item
argument_list|>
name|unlikeItems
parameter_list|,
name|boolean
name|include
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|moreLikeFields
parameter_list|,
name|boolean
name|useDefaultField
parameter_list|)
throws|throws
name|IOException
block|{
comment|// set default index, type and fields if not specified
for|for
control|(
name|Item
name|item
range|:
name|likeItems
control|)
block|{
name|setDefaultIndexTypeFields
argument_list|(
name|parseContext
argument_list|,
name|item
argument_list|,
name|moreLikeFields
argument_list|,
name|useDefaultField
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Item
name|item
range|:
name|unlikeItems
control|)
block|{
name|setDefaultIndexTypeFields
argument_list|(
name|parseContext
argument_list|,
name|item
argument_list|,
name|moreLikeFields
argument_list|,
name|useDefaultField
argument_list|)
expr_stmt|;
block|}
comment|// fetching the items with multi-termvectors API
name|MultiTermVectorsResponse
name|responses
init|=
name|fetchService
operator|.
name|fetchResponse
argument_list|(
name|likeItems
argument_list|,
name|unlikeItems
argument_list|,
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|)
decl_stmt|;
comment|// getting the Fields for liked items
name|mltQuery
operator|.
name|setLikeText
argument_list|(
name|MoreLikeThisFetchService
operator|.
name|getFieldsFor
argument_list|(
name|responses
argument_list|,
name|likeItems
argument_list|)
argument_list|)
expr_stmt|;
comment|// getting the Fields for unliked items
if|if
condition|(
operator|!
name|unlikeItems
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Fields
index|[]
name|unlikeFields
init|=
name|MoreLikeThisFetchService
operator|.
name|getFieldsFor
argument_list|(
name|responses
argument_list|,
name|unlikeItems
argument_list|)
decl_stmt|;
if|if
condition|(
name|unlikeFields
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|mltQuery
operator|.
name|setUnlikeText
argument_list|(
name|unlikeFields
argument_list|)
expr_stmt|;
block|}
block|}
name|BooleanQuery
name|boolQuery
init|=
operator|new
name|BooleanQuery
argument_list|()
decl_stmt|;
name|boolQuery
operator|.
name|add
argument_list|(
name|mltQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
comment|// exclude the items from the search
if|if
condition|(
operator|!
name|include
condition|)
block|{
name|handleExclude
argument_list|(
name|boolQuery
argument_list|,
name|likeItems
argument_list|)
expr_stmt|;
block|}
return|return
name|boolQuery
return|;
block|}
DECL|method|setDefaultIndexTypeFields
specifier|private
specifier|static
name|void
name|setDefaultIndexTypeFields
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|Item
name|item
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|moreLikeFields
parameter_list|,
name|boolean
name|useDefaultField
parameter_list|)
block|{
if|if
condition|(
name|item
operator|.
name|index
argument_list|()
operator|==
literal|null
condition|)
block|{
name|item
operator|.
name|index
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|item
operator|.
name|type
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|queryTypes
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"ambiguous type for item with id: "
operator|+
name|item
operator|.
name|id
argument_list|()
operator|+
literal|" and index: "
operator|+
name|item
operator|.
name|index
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|item
operator|.
name|type
argument_list|(
name|parseContext
operator|.
name|queryTypes
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// default fields if not present but don't override for artificial docs
if|if
condition|(
operator|(
name|item
operator|.
name|fields
argument_list|()
operator|==
literal|null
operator|||
name|item
operator|.
name|fields
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|)
operator|&&
name|item
operator|.
name|doc
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|useDefaultField
condition|)
block|{
name|item
operator|.
name|fields
argument_list|(
literal|"*"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|item
operator|.
name|fields
argument_list|(
name|moreLikeFields
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|moreLikeFields
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|handleExclude
specifier|private
specifier|static
name|void
name|handleExclude
parameter_list|(
name|BooleanQuery
name|boolQuery
parameter_list|,
name|List
argument_list|<
name|Item
argument_list|>
name|likeItems
parameter_list|)
block|{
comment|// artificial docs get assigned a random id and should be disregarded
name|List
argument_list|<
name|BytesRef
argument_list|>
name|uids
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Item
name|item
range|:
name|likeItems
control|)
block|{
if|if
condition|(
name|item
operator|.
name|doc
argument_list|()
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
name|uids
operator|.
name|add
argument_list|(
name|createUidAsBytes
argument_list|(
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|item
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|uids
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|TermsQuery
name|query
init|=
operator|new
name|TermsQuery
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|uids
operator|.
name|toArray
argument_list|(
operator|new
name|BytesRef
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|boolQuery
operator|.
name|add
argument_list|(
name|query
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

