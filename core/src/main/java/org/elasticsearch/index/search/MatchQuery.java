begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
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
name|miscellaneous
operator|.
name|DisableGraphAttribute
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
name|BoostQuery
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
name|FuzzyQuery
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
name|MultiPhraseQuery
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
name|MultiTermQuery
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
name|PhraseQuery
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
name|PrefixQuery
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
name|SynonymQuery
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
name|TermQuery
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
name|spans
operator|.
name|SpanMultiTermQueryWrapper
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
name|spans
operator|.
name|SpanNearQuery
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
name|spans
operator|.
name|SpanOrQuery
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
name|spans
operator|.
name|SpanQuery
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
name|spans
operator|.
name|SpanTermQuery
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
name|QueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|all
operator|.
name|AllTermQuery
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
name|MultiPhrasePrefixQuery
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
name|Queries
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
name|unit
operator|.
name|Fuzziness
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
name|ShingleTokenFilterFactory
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
name|query
operator|.
name|QueryShardContext
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
name|support
operator|.
name|QueryParsers
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

begin_class
DECL|class|MatchQuery
specifier|public
class|class
name|MatchQuery
block|{
DECL|enum|Type
specifier|public
enum|enum
name|Type
implements|implements
name|Writeable
block|{
comment|/**          * The text is analyzed and terms are added to a boolean query.          */
DECL|enum constant|BOOLEAN
name|BOOLEAN
argument_list|(
literal|0
argument_list|)
block|,
comment|/**          * The text is analyzed and used as a phrase query.          */
DECL|enum constant|PHRASE
name|PHRASE
argument_list|(
literal|1
argument_list|)
block|,
comment|/**          * The text is analyzed and used in a phrase query, with the last term acting as a prefix.          */
DECL|enum constant|PHRASE_PREFIX
name|PHRASE_PREFIX
argument_list|(
literal|2
argument_list|)
block|;
DECL|field|ordinal
specifier|private
specifier|final
name|int
name|ordinal
decl_stmt|;
DECL|method|Type
name|Type
parameter_list|(
name|int
name|ordinal
parameter_list|)
block|{
name|this
operator|.
name|ordinal
operator|=
name|ordinal
expr_stmt|;
block|}
DECL|method|readFromStream
specifier|public
specifier|static
name|Type
name|readFromStream
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ord
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|type
operator|.
name|ordinal
operator|==
name|ord
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"unknown serialized type ["
operator|+
name|ord
operator|+
literal|"]"
argument_list|)
throw|;
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
name|writeVInt
argument_list|(
name|this
operator|.
name|ordinal
argument_list|)
expr_stmt|;
block|}
block|}
DECL|enum|ZeroTermsQuery
specifier|public
enum|enum
name|ZeroTermsQuery
implements|implements
name|Writeable
block|{
DECL|enum constant|NONE
name|NONE
argument_list|(
literal|0
argument_list|)
block|,
DECL|enum constant|ALL
name|ALL
argument_list|(
literal|1
argument_list|)
block|;
DECL|field|ordinal
specifier|private
specifier|final
name|int
name|ordinal
decl_stmt|;
DECL|method|ZeroTermsQuery
name|ZeroTermsQuery
parameter_list|(
name|int
name|ordinal
parameter_list|)
block|{
name|this
operator|.
name|ordinal
operator|=
name|ordinal
expr_stmt|;
block|}
DECL|method|readFromStream
specifier|public
specifier|static
name|ZeroTermsQuery
name|readFromStream
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ord
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|ZeroTermsQuery
name|zeroTermsQuery
range|:
name|ZeroTermsQuery
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|zeroTermsQuery
operator|.
name|ordinal
operator|==
name|ord
condition|)
block|{
return|return
name|zeroTermsQuery
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"unknown serialized type ["
operator|+
name|ord
operator|+
literal|"]"
argument_list|)
throw|;
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
name|writeVInt
argument_list|(
name|this
operator|.
name|ordinal
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * the default phrase slop      */
DECL|field|DEFAULT_PHRASE_SLOP
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_PHRASE_SLOP
init|=
literal|0
decl_stmt|;
comment|/**      * the default leniency setting      */
DECL|field|DEFAULT_LENIENCY
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_LENIENCY
init|=
literal|false
decl_stmt|;
comment|/**      * the default zero terms query      */
DECL|field|DEFAULT_ZERO_TERMS_QUERY
specifier|public
specifier|static
specifier|final
name|ZeroTermsQuery
name|DEFAULT_ZERO_TERMS_QUERY
init|=
name|ZeroTermsQuery
operator|.
name|NONE
decl_stmt|;
DECL|field|context
specifier|protected
specifier|final
name|QueryShardContext
name|context
decl_stmt|;
DECL|field|analyzer
specifier|protected
name|String
name|analyzer
decl_stmt|;
DECL|field|occur
specifier|protected
name|BooleanClause
operator|.
name|Occur
name|occur
init|=
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|protected
name|boolean
name|enablePositionIncrements
init|=
literal|true
decl_stmt|;
DECL|field|phraseSlop
specifier|protected
name|int
name|phraseSlop
init|=
name|DEFAULT_PHRASE_SLOP
decl_stmt|;
DECL|field|fuzziness
specifier|protected
name|Fuzziness
name|fuzziness
init|=
literal|null
decl_stmt|;
DECL|field|fuzzyPrefixLength
specifier|protected
name|int
name|fuzzyPrefixLength
init|=
name|FuzzyQuery
operator|.
name|defaultPrefixLength
decl_stmt|;
DECL|field|maxExpansions
specifier|protected
name|int
name|maxExpansions
init|=
name|FuzzyQuery
operator|.
name|defaultMaxExpansions
decl_stmt|;
DECL|field|transpositions
specifier|protected
name|boolean
name|transpositions
init|=
name|FuzzyQuery
operator|.
name|defaultTranspositions
decl_stmt|;
DECL|field|fuzzyRewriteMethod
specifier|protected
name|MultiTermQuery
operator|.
name|RewriteMethod
name|fuzzyRewriteMethod
decl_stmt|;
DECL|field|lenient
specifier|protected
name|boolean
name|lenient
init|=
name|DEFAULT_LENIENCY
decl_stmt|;
DECL|field|zeroTermsQuery
specifier|protected
name|ZeroTermsQuery
name|zeroTermsQuery
init|=
name|DEFAULT_ZERO_TERMS_QUERY
decl_stmt|;
DECL|field|commonTermsCutoff
specifier|protected
name|Float
name|commonTermsCutoff
init|=
literal|null
decl_stmt|;
DECL|method|MatchQuery
specifier|public
name|MatchQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
DECL|method|setAnalyzer
specifier|public
name|void
name|setAnalyzer
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
block|}
DECL|method|setOccur
specifier|public
name|void
name|setOccur
parameter_list|(
name|BooleanClause
operator|.
name|Occur
name|occur
parameter_list|)
block|{
name|this
operator|.
name|occur
operator|=
name|occur
expr_stmt|;
block|}
DECL|method|setCommonTermsCutoff
specifier|public
name|void
name|setCommonTermsCutoff
parameter_list|(
name|Float
name|cutoff
parameter_list|)
block|{
name|this
operator|.
name|commonTermsCutoff
operator|=
name|cutoff
expr_stmt|;
block|}
DECL|method|setEnablePositionIncrements
specifier|public
name|void
name|setEnablePositionIncrements
parameter_list|(
name|boolean
name|enablePositionIncrements
parameter_list|)
block|{
name|this
operator|.
name|enablePositionIncrements
operator|=
name|enablePositionIncrements
expr_stmt|;
block|}
DECL|method|setPhraseSlop
specifier|public
name|void
name|setPhraseSlop
parameter_list|(
name|int
name|phraseSlop
parameter_list|)
block|{
name|this
operator|.
name|phraseSlop
operator|=
name|phraseSlop
expr_stmt|;
block|}
DECL|method|setFuzziness
specifier|public
name|void
name|setFuzziness
parameter_list|(
name|Fuzziness
name|fuzziness
parameter_list|)
block|{
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
block|}
DECL|method|setFuzzyPrefixLength
specifier|public
name|void
name|setFuzzyPrefixLength
parameter_list|(
name|int
name|fuzzyPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|fuzzyPrefixLength
operator|=
name|fuzzyPrefixLength
expr_stmt|;
block|}
DECL|method|setMaxExpansions
specifier|public
name|void
name|setMaxExpansions
parameter_list|(
name|int
name|maxExpansions
parameter_list|)
block|{
name|this
operator|.
name|maxExpansions
operator|=
name|maxExpansions
expr_stmt|;
block|}
DECL|method|setTranspositions
specifier|public
name|void
name|setTranspositions
parameter_list|(
name|boolean
name|transpositions
parameter_list|)
block|{
name|this
operator|.
name|transpositions
operator|=
name|transpositions
expr_stmt|;
block|}
DECL|method|setFuzzyRewriteMethod
specifier|public
name|void
name|setFuzzyRewriteMethod
parameter_list|(
name|MultiTermQuery
operator|.
name|RewriteMethod
name|fuzzyRewriteMethod
parameter_list|)
block|{
name|this
operator|.
name|fuzzyRewriteMethod
operator|=
name|fuzzyRewriteMethod
expr_stmt|;
block|}
DECL|method|setLenient
specifier|public
name|void
name|setLenient
parameter_list|(
name|boolean
name|lenient
parameter_list|)
block|{
name|this
operator|.
name|lenient
operator|=
name|lenient
expr_stmt|;
block|}
DECL|method|setZeroTermsQuery
specifier|public
name|void
name|setZeroTermsQuery
parameter_list|(
name|ZeroTermsQuery
name|zeroTermsQuery
parameter_list|)
block|{
name|this
operator|.
name|zeroTermsQuery
operator|=
name|zeroTermsQuery
expr_stmt|;
block|}
DECL|method|getAnalyzer
specifier|protected
name|Analyzer
name|getAnalyzer
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
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
return|return
name|context
operator|.
name|getSearchAnalyzer
argument_list|(
name|fieldType
argument_list|)
return|;
block|}
return|return
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
return|;
block|}
else|else
block|{
name|Analyzer
name|analyzer
init|=
name|context
operator|.
name|getMapperService
argument_list|()
operator|.
name|getIndexAnalyzers
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|analyzer
argument_list|)
decl_stmt|;
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
name|this
operator|.
name|analyzer
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|analyzer
return|;
block|}
block|}
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
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
comment|/*          * If the user forced an analyzer we really don't care if they are          * searching a type that wants term queries to be used with query string          * because the QueryBuilder will take care of it. If they haven't forced          * an analyzer then types like NumberFieldType that want terms with          * query string will blow up because their analyzer isn't capable of          * passing through QueryBuilder.          */
name|boolean
name|noForcedAnalyzer
init|=
name|this
operator|.
name|analyzer
operator|==
literal|null
decl_stmt|;
if|if
condition|(
name|fieldType
operator|!=
literal|null
operator|&&
name|fieldType
operator|.
name|tokenized
argument_list|()
operator|==
literal|false
operator|&&
name|noForcedAnalyzer
condition|)
block|{
return|return
name|blendTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|fieldName
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
name|Analyzer
name|analyzer
init|=
name|getAnalyzer
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
assert|assert
name|analyzer
operator|!=
literal|null
assert|;
name|MatchQueryBuilder
name|builder
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
name|analyzer
argument_list|,
name|fieldType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setEnablePositionIncrements
argument_list|(
name|this
operator|.
name|enablePositionIncrements
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN
case|:
if|if
condition|(
name|commonTermsCutoff
operator|==
literal|null
condition|)
block|{
name|query
operator|=
name|builder
operator|.
name|createBooleanQuery
argument_list|(
name|field
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|,
name|occur
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
name|builder
operator|.
name|createCommonTermsQuery
argument_list|(
name|field
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|,
name|occur
argument_list|,
name|occur
argument_list|,
name|commonTermsCutoff
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|PHRASE
case|:
name|query
operator|=
name|builder
operator|.
name|createPhraseQuery
argument_list|(
name|field
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|,
name|phraseSlop
argument_list|)
expr_stmt|;
break|break;
case|case
name|PHRASE_PREFIX
case|:
name|query
operator|=
name|builder
operator|.
name|createPhrasePrefixQuery
argument_list|(
name|field
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|,
name|phraseSlop
argument_list|,
name|maxExpansions
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"No type found for ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
return|return
name|zeroTermsQuery
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|query
return|;
block|}
block|}
DECL|method|termQuery
specifier|protected
specifier|final
name|Query
name|termQuery
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|,
name|Object
name|value
parameter_list|,
name|boolean
name|lenient
parameter_list|)
block|{
try|try
block|{
return|return
name|fieldType
operator|.
name|termQuery
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
if|if
condition|(
name|lenient
condition|)
block|{
return|return
literal|null
return|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
DECL|method|zeroTermsQuery
specifier|protected
name|Query
name|zeroTermsQuery
parameter_list|()
block|{
if|if
condition|(
name|zeroTermsQuery
operator|==
name|DEFAULT_ZERO_TERMS_QUERY
condition|)
block|{
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|(
literal|"Matching no documents because no terms present."
argument_list|)
return|;
block|}
return|return
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
return|;
block|}
DECL|class|MatchQueryBuilder
specifier|private
class|class
name|MatchQueryBuilder
extends|extends
name|QueryBuilder
block|{
DECL|field|mapper
specifier|private
specifier|final
name|MappedFieldType
name|mapper
decl_stmt|;
comment|/**          * Creates a new QueryBuilder using the given analyzer.          */
DECL|method|MatchQueryBuilder
name|MatchQueryBuilder
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
annotation|@
name|Nullable
name|MappedFieldType
name|mapper
parameter_list|)
block|{
name|super
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
name|this
operator|.
name|mapper
operator|=
name|mapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newTermQuery
specifier|protected
name|Query
name|newTermQuery
parameter_list|(
name|Term
name|term
parameter_list|)
block|{
return|return
name|blendTermQuery
argument_list|(
name|term
argument_list|,
name|mapper
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newSynonymQuery
specifier|protected
name|Query
name|newSynonymQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|)
block|{
return|return
name|blendTermsQuery
argument_list|(
name|terms
argument_list|,
name|mapper
argument_list|)
return|;
block|}
comment|/**          * Checks if graph analysis should be enabled for the field depending          * on the provided {@link Analyzer}          */
DECL|method|createFieldQuery
specifier|protected
name|Query
name|createFieldQuery
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|BooleanClause
operator|.
name|Occur
name|operator
parameter_list|,
name|String
name|field
parameter_list|,
name|String
name|queryText
parameter_list|,
name|boolean
name|quoted
parameter_list|,
name|int
name|phraseSlop
parameter_list|)
block|{
assert|assert
name|operator
operator|==
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
operator|||
name|operator
operator|==
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
assert|;
comment|// Use the analyzer to get all the tokens, and then build an appropriate
comment|// query based on the analysis chain.
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
name|queryText
argument_list|)
init|)
block|{
if|if
condition|(
name|source
operator|.
name|hasAttribute
argument_list|(
name|DisableGraphAttribute
operator|.
name|class
argument_list|)
condition|)
block|{
comment|/**                      * A {@link TokenFilter} in this {@link TokenStream} disabled the graph analysis to avoid                      * paths explosion. See {@link ShingleTokenFilterFactory} for details.                      */
name|setEnableGraphQueries
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
init|=
name|super
operator|.
name|createFieldQuery
argument_list|(
name|source
argument_list|,
name|operator
argument_list|,
name|field
argument_list|,
name|quoted
argument_list|,
name|phraseSlop
argument_list|)
decl_stmt|;
name|setEnableGraphQueries
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|query
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
name|RuntimeException
argument_list|(
literal|"Error analyzing query text"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|createPhrasePrefixQuery
specifier|public
name|Query
name|createPhrasePrefixQuery
parameter_list|(
name|String
name|field
parameter_list|,
name|String
name|queryText
parameter_list|,
name|int
name|phraseSlop
parameter_list|,
name|int
name|maxExpansions
parameter_list|)
block|{
specifier|final
name|Query
name|query
init|=
name|createFieldQuery
argument_list|(
name|getAnalyzer
argument_list|()
argument_list|,
name|Occur
operator|.
name|MUST
argument_list|,
name|field
argument_list|,
name|queryText
argument_list|,
literal|true
argument_list|,
name|phraseSlop
argument_list|)
decl_stmt|;
return|return
name|toMultiPhrasePrefix
argument_list|(
name|query
argument_list|,
name|phraseSlop
argument_list|,
name|maxExpansions
argument_list|)
return|;
block|}
DECL|method|toMultiPhrasePrefix
specifier|private
name|Query
name|toMultiPhrasePrefix
parameter_list|(
specifier|final
name|Query
name|query
parameter_list|,
name|int
name|phraseSlop
parameter_list|,
name|int
name|maxExpansions
parameter_list|)
block|{
name|float
name|boost
init|=
literal|1
decl_stmt|;
name|Query
name|innerQuery
init|=
name|query
decl_stmt|;
while|while
condition|(
name|innerQuery
operator|instanceof
name|BoostQuery
condition|)
block|{
name|BoostQuery
name|bq
init|=
operator|(
name|BoostQuery
operator|)
name|innerQuery
decl_stmt|;
name|boost
operator|*=
name|bq
operator|.
name|getBoost
argument_list|()
expr_stmt|;
name|innerQuery
operator|=
name|bq
operator|.
name|getQuery
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|instanceof
name|SpanQuery
condition|)
block|{
return|return
name|toSpanQueryPrefix
argument_list|(
operator|(
name|SpanQuery
operator|)
name|query
argument_list|,
name|boost
argument_list|)
return|;
block|}
specifier|final
name|MultiPhrasePrefixQuery
name|prefixQuery
init|=
operator|new
name|MultiPhrasePrefixQuery
argument_list|()
decl_stmt|;
name|prefixQuery
operator|.
name|setMaxExpansions
argument_list|(
name|maxExpansions
argument_list|)
expr_stmt|;
name|prefixQuery
operator|.
name|setSlop
argument_list|(
name|phraseSlop
argument_list|)
expr_stmt|;
if|if
condition|(
name|innerQuery
operator|instanceof
name|PhraseQuery
condition|)
block|{
name|PhraseQuery
name|pq
init|=
operator|(
name|PhraseQuery
operator|)
name|innerQuery
decl_stmt|;
name|Term
index|[]
name|terms
init|=
name|pq
operator|.
name|getTerms
argument_list|()
decl_stmt|;
name|int
index|[]
name|positions
init|=
name|pq
operator|.
name|getPositions
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|prefixQuery
operator|.
name|add
argument_list|(
operator|new
name|Term
index|[]
block|{
name|terms
index|[
name|i
index|]
block|}
argument_list|,
name|positions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|boost
operator|==
literal|1
condition|?
name|prefixQuery
else|:
operator|new
name|BoostQuery
argument_list|(
name|prefixQuery
argument_list|,
name|boost
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|innerQuery
operator|instanceof
name|MultiPhraseQuery
condition|)
block|{
name|MultiPhraseQuery
name|pq
init|=
operator|(
name|MultiPhraseQuery
operator|)
name|innerQuery
decl_stmt|;
name|Term
index|[]
index|[]
name|terms
init|=
name|pq
operator|.
name|getTermArrays
argument_list|()
decl_stmt|;
name|int
index|[]
name|positions
init|=
name|pq
operator|.
name|getPositions
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|prefixQuery
operator|.
name|add
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|positions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|boost
operator|==
literal|1
condition|?
name|prefixQuery
else|:
operator|new
name|BoostQuery
argument_list|(
name|prefixQuery
argument_list|,
name|boost
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|innerQuery
operator|instanceof
name|TermQuery
condition|)
block|{
name|prefixQuery
operator|.
name|add
argument_list|(
operator|(
operator|(
name|TermQuery
operator|)
name|innerQuery
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|boost
operator|==
literal|1
condition|?
name|prefixQuery
else|:
operator|new
name|BoostQuery
argument_list|(
name|prefixQuery
argument_list|,
name|boost
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|innerQuery
operator|instanceof
name|AllTermQuery
condition|)
block|{
name|prefixQuery
operator|.
name|add
argument_list|(
operator|(
operator|(
name|AllTermQuery
operator|)
name|innerQuery
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|boost
operator|==
literal|1
condition|?
name|prefixQuery
else|:
operator|new
name|BoostQuery
argument_list|(
name|prefixQuery
argument_list|,
name|boost
argument_list|)
return|;
block|}
return|return
name|query
return|;
block|}
DECL|method|toSpanQueryPrefix
specifier|private
name|Query
name|toSpanQueryPrefix
parameter_list|(
name|SpanQuery
name|query
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
if|if
condition|(
name|query
operator|instanceof
name|SpanTermQuery
condition|)
block|{
name|SpanMultiTermQueryWrapper
argument_list|<
name|PrefixQuery
argument_list|>
name|ret
init|=
operator|new
name|SpanMultiTermQueryWrapper
argument_list|<>
argument_list|(
operator|new
name|PrefixQuery
argument_list|(
operator|(
operator|(
name|SpanTermQuery
operator|)
name|query
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|boost
operator|==
literal|1
condition|?
name|ret
else|:
operator|new
name|BoostQuery
argument_list|(
name|ret
argument_list|,
name|boost
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanNearQuery
condition|)
block|{
name|SpanNearQuery
name|spanNearQuery
init|=
operator|(
name|SpanNearQuery
operator|)
name|query
decl_stmt|;
name|SpanQuery
index|[]
name|clauses
init|=
name|spanNearQuery
operator|.
name|getClauses
argument_list|()
decl_stmt|;
if|if
condition|(
name|clauses
index|[
name|clauses
operator|.
name|length
operator|-
literal|1
index|]
operator|instanceof
name|SpanTermQuery
condition|)
block|{
name|clauses
index|[
name|clauses
operator|.
name|length
operator|-
literal|1
index|]
operator|=
operator|new
name|SpanMultiTermQueryWrapper
argument_list|<>
argument_list|(
operator|new
name|PrefixQuery
argument_list|(
operator|(
operator|(
name|SpanTermQuery
operator|)
name|clauses
index|[
name|clauses
operator|.
name|length
operator|-
literal|1
index|]
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SpanNearQuery
name|newQuery
init|=
operator|new
name|SpanNearQuery
argument_list|(
name|clauses
argument_list|,
name|spanNearQuery
operator|.
name|getSlop
argument_list|()
argument_list|,
name|spanNearQuery
operator|.
name|isInOrder
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|boost
operator|==
literal|1
condition|?
name|newQuery
else|:
operator|new
name|BoostQuery
argument_list|(
name|newQuery
argument_list|,
name|boost
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanOrQuery
condition|)
block|{
name|SpanOrQuery
name|orQuery
init|=
operator|(
name|SpanOrQuery
operator|)
name|query
decl_stmt|;
name|SpanQuery
index|[]
name|clauses
init|=
operator|new
name|SpanQuery
index|[
name|orQuery
operator|.
name|getClauses
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|clauses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|clauses
index|[
name|i
index|]
operator|=
operator|(
name|SpanQuery
operator|)
name|toSpanQueryPrefix
argument_list|(
name|orQuery
operator|.
name|getClauses
argument_list|()
index|[
name|i
index|]
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|boost
operator|==
literal|1
condition|?
operator|new
name|SpanOrQuery
argument_list|(
name|clauses
argument_list|)
else|:
operator|new
name|BoostQuery
argument_list|(
operator|new
name|SpanOrQuery
argument_list|(
name|clauses
argument_list|)
argument_list|,
name|boost
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|query
return|;
block|}
block|}
DECL|method|createCommonTermsQuery
specifier|public
name|Query
name|createCommonTermsQuery
parameter_list|(
name|String
name|field
parameter_list|,
name|String
name|queryText
parameter_list|,
name|Occur
name|highFreqOccur
parameter_list|,
name|Occur
name|lowFreqOccur
parameter_list|,
name|float
name|maxTermFrequency
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|Query
name|booleanQuery
init|=
name|createBooleanQuery
argument_list|(
name|field
argument_list|,
name|queryText
argument_list|,
name|lowFreqOccur
argument_list|)
decl_stmt|;
if|if
condition|(
name|booleanQuery
operator|!=
literal|null
operator|&&
name|booleanQuery
operator|instanceof
name|BooleanQuery
condition|)
block|{
name|BooleanQuery
name|bq
init|=
operator|(
name|BooleanQuery
operator|)
name|booleanQuery
decl_stmt|;
return|return
name|boolToExtendedCommonTermsQuery
argument_list|(
name|bq
argument_list|,
name|highFreqOccur
argument_list|,
name|lowFreqOccur
argument_list|,
name|maxTermFrequency
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
return|return
name|booleanQuery
return|;
block|}
DECL|method|boolToExtendedCommonTermsQuery
specifier|private
name|Query
name|boolToExtendedCommonTermsQuery
parameter_list|(
name|BooleanQuery
name|bq
parameter_list|,
name|Occur
name|highFreqOccur
parameter_list|,
name|Occur
name|lowFreqOccur
parameter_list|,
name|float
name|maxTermFrequency
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|ExtendedCommonTermsQuery
name|query
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
name|fieldType
argument_list|)
decl_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|bq
operator|.
name|clauses
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|clause
operator|.
name|getQuery
argument_list|()
operator|instanceof
name|TermQuery
operator|)
condition|)
block|{
return|return
name|bq
return|;
block|}
name|query
operator|.
name|add
argument_list|(
operator|(
operator|(
name|TermQuery
operator|)
name|clause
operator|.
name|getQuery
argument_list|()
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
block|}
DECL|method|blendTermsQuery
specifier|protected
name|Query
name|blendTermsQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
return|return
operator|new
name|SynonymQuery
argument_list|(
name|terms
argument_list|)
return|;
block|}
DECL|method|blendTermQuery
specifier|protected
name|Query
name|blendTermQuery
parameter_list|(
name|Term
name|term
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|fuzziness
operator|!=
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
try|try
block|{
name|Query
name|query
init|=
name|fieldType
operator|.
name|fuzzyQuery
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|,
name|fuzziness
argument_list|,
name|fuzzyPrefixLength
argument_list|,
name|maxExpansions
argument_list|,
name|transpositions
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|instanceof
name|FuzzyQuery
condition|)
block|{
name|QueryParsers
operator|.
name|setRewriteMethod
argument_list|(
operator|(
name|FuzzyQuery
operator|)
name|query
argument_list|,
name|fuzzyRewriteMethod
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
if|if
condition|(
name|lenient
condition|)
block|{
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
name|int
name|edits
init|=
name|fuzziness
operator|.
name|asDistance
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|FuzzyQuery
name|query
init|=
operator|new
name|FuzzyQuery
argument_list|(
name|term
argument_list|,
name|edits
argument_list|,
name|fuzzyPrefixLength
argument_list|,
name|maxExpansions
argument_list|,
name|transpositions
argument_list|)
decl_stmt|;
name|QueryParsers
operator|.
name|setRewriteMethod
argument_list|(
name|query
argument_list|,
name|fuzzyRewriteMethod
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|Query
name|query
init|=
name|termQuery
argument_list|(
name|fieldType
argument_list|,
name|term
operator|.
name|bytes
argument_list|()
argument_list|,
name|lenient
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
return|return
name|query
return|;
block|}
block|}
return|return
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|)
return|;
block|}
block|}
end_class

end_unit

