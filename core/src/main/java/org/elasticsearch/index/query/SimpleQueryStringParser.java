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
name|common
operator|.
name|util
operator|.
name|LocaleUtils
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * SimpleQueryStringParser is a query parser that acts similar to a query_string  * query, but won't throw exceptions for any weird string syntax. It supports  * the following:  *<p/>  *<ul>  *<li>'{@code +}' specifies {@code AND} operation:<tt>token1+token2</tt>  *<li>'{@code |}' specifies {@code OR} operation:<tt>token1|token2</tt>  *<li>'{@code -}' negates a single token:<tt>-token0</tt>  *<li>'{@code "}' creates phrases of terms:<tt>"term1 term2 ..."</tt>  *<li>'{@code *}' at the end of terms specifies prefix query:<tt>term*</tt>  *<li>'{@code (}' and '{@code)}' specifies precedence:<tt>token1 + (token2 | token3)</tt>  *<li>'{@code ~}N' at the end of terms specifies fuzzy query:<tt>term~1</tt>  *<li>'{@code ~}N' at the end of phrases specifies near/slop query:<tt>"term1 term2"~5</tt>  *</ul>  *<p/>  * See: {@link XSimpleQueryParser} for more information.  *<p/>  * This query supports these options:  *<p/>  * Required:  * {@code query} - query text to be converted into other queries  *<p/>  * Optional:  * {@code analyzer} - anaylzer to be used for analyzing tokens to determine  * which kind of query they should be converted into, defaults to "standard"  * {@code default_operator} - default operator for boolean queries, defaults  * to OR  * {@code fields} - fields to search, defaults to _all if not set, allows  * boosting a field with ^n  */
end_comment

begin_class
DECL|class|SimpleQueryStringParser
specifier|public
class|class
name|SimpleQueryStringParser
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
literal|"simple_query_string"
decl_stmt|;
annotation|@
name|Inject
DECL|method|SimpleQueryStringParser
specifier|public
name|SimpleQueryStringParser
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{      }
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
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|NAME
argument_list|)
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
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|String
name|queryBody
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|field
init|=
literal|null
decl_stmt|;
name|String
name|minimumShouldMatch
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fieldsAndWeights
init|=
literal|null
decl_stmt|;
name|BooleanClause
operator|.
name|Occur
name|defaultOperator
init|=
literal|null
decl_stmt|;
name|Analyzer
name|analyzer
init|=
literal|null
decl_stmt|;
name|int
name|flags
init|=
operator|-
literal|1
decl_stmt|;
name|SimpleQueryParser
operator|.
name|Settings
name|sqsSettings
init|=
operator|new
name|SimpleQueryParser
operator|.
name|Settings
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
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
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"fields"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
name|String
name|fField
init|=
literal|null
decl_stmt|;
name|float
name|fBoost
init|=
literal|1
decl_stmt|;
name|char
index|[]
name|text
init|=
name|parser
operator|.
name|textCharacters
argument_list|()
decl_stmt|;
name|int
name|end
init|=
name|parser
operator|.
name|textOffset
argument_list|()
operator|+
name|parser
operator|.
name|textLength
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|parser
operator|.
name|textOffset
argument_list|()
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|text
index|[
name|i
index|]
operator|==
literal|'^'
condition|)
block|{
name|int
name|relativeLocation
init|=
name|i
operator|-
name|parser
operator|.
name|textOffset
argument_list|()
decl_stmt|;
name|fField
operator|=
operator|new
name|String
argument_list|(
name|text
argument_list|,
name|parser
operator|.
name|textOffset
argument_list|()
argument_list|,
name|relativeLocation
argument_list|)
expr_stmt|;
name|fBoost
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
operator|new
name|String
argument_list|(
name|text
argument_list|,
name|i
operator|+
literal|1
argument_list|,
name|parser
operator|.
name|textLength
argument_list|()
operator|-
name|relativeLocation
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|fField
operator|==
literal|null
condition|)
block|{
name|fField
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fieldsAndWeights
operator|==
literal|null
condition|)
block|{
name|fieldsAndWeights
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|Regex
operator|.
name|isSimpleMatchPattern
argument_list|(
name|fField
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|fieldName
range|:
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|simpleMatchToIndexNames
argument_list|(
name|fField
argument_list|)
control|)
block|{
name|fieldsAndWeights
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|MappedFieldType
name|fieldType
init|=
name|parseContext
operator|.
name|fieldMapper
argument_list|(
name|fField
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|fieldsAndWeights
operator|.
name|put
argument_list|(
name|fieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldsAndWeights
operator|.
name|put
argument_list|(
name|fField
argument_list|,
name|fBoost
argument_list|)
expr_stmt|;
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
literal|"["
operator|+
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
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queryBody
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
literal|"analyzer"
operator|.
name|equals
argument_list|(
name|currentFieldName
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
if|if
condition|(
name|analyzer
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
literal|"["
operator|+
name|NAME
operator|+
literal|"] analyzer ["
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
block|}
elseif|else
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|field
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
literal|"default_operator"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"defaultOperator"
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
name|defaultOperator
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
name|defaultOperator
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
literal|"["
operator|+
name|NAME
operator|+
literal|"] default operator ["
operator|+
name|op
operator|+
literal|"] is not allowed"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"flags"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
comment|// Possible options are:
comment|// ALL, NONE, AND, OR, PREFIX, PHRASE, PRECEDENCE, ESCAPE, WHITESPACE, FUZZY, NEAR, SLOP
name|flags
operator|=
name|SimpleQueryStringFlag
operator|.
name|resolveFlags
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|flags
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|flags
operator|<
literal|0
condition|)
block|{
name|flags
operator|=
name|SimpleQueryStringFlag
operator|.
name|ALL
operator|.
name|value
argument_list|()
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
literal|"locale"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|localeStr
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|Locale
name|locale
init|=
name|LocaleUtils
operator|.
name|parse
argument_list|(
name|localeStr
argument_list|)
decl_stmt|;
name|sqsSettings
operator|.
name|locale
argument_list|(
name|locale
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lowercase_expanded_terms"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|sqsSettings
operator|.
name|lowercaseExpandedTerms
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lenient"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|sqsSettings
operator|.
name|lenient
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"analyze_wildcard"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|sqsSettings
operator|.
name|analyzeWildcard
argument_list|(
name|parser
operator|.
name|booleanValue
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
elseif|else
if|if
condition|(
literal|"minimum_should_match"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|minimumShouldMatch
operator|=
name|parser
operator|.
name|textOrNull
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
literal|"["
operator|+
name|NAME
operator|+
literal|"] unsupported field ["
operator|+
name|parser
operator|.
name|currentName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Query text is required
if|if
condition|(
name|queryBody
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
literal|"["
operator|+
name|NAME
operator|+
literal|"] query text missing"
argument_list|)
throw|;
block|}
comment|// Support specifying only a field instead of a map
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
name|field
operator|=
name|currentFieldName
expr_stmt|;
block|}
comment|// Use the default field (_all) if no fields specified
if|if
condition|(
name|fieldsAndWeights
operator|==
literal|null
condition|)
block|{
name|field
operator|=
name|parseContext
operator|.
name|defaultField
argument_list|()
expr_stmt|;
block|}
comment|// Use standard analyzer by default
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
if|if
condition|(
name|fieldsAndWeights
operator|==
literal|null
condition|)
block|{
name|fieldsAndWeights
operator|=
name|Collections
operator|.
name|singletonMap
argument_list|(
name|field
argument_list|,
literal|1.0F
argument_list|)
expr_stmt|;
block|}
name|SimpleQueryParser
name|sqp
init|=
operator|new
name|SimpleQueryParser
argument_list|(
name|analyzer
argument_list|,
name|fieldsAndWeights
argument_list|,
name|flags
argument_list|,
name|sqsSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultOperator
operator|!=
literal|null
condition|)
block|{
name|sqp
operator|.
name|setDefaultOperator
argument_list|(
name|defaultOperator
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
init|=
name|sqp
operator|.
name|parse
argument_list|(
name|queryBody
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
if|if
condition|(
name|minimumShouldMatch
operator|!=
literal|null
operator|&&
name|query
operator|instanceof
name|BooleanQuery
condition|)
block|{
name|Queries
operator|.
name|applyMinimumShouldMatch
argument_list|(
operator|(
name|BooleanQuery
operator|)
name|query
argument_list|,
name|minimumShouldMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|query
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

