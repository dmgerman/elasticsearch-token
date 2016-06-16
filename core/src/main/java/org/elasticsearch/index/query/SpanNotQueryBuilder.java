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
name|spans
operator|.
name|SpanNotQuery
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

begin_class
DECL|class|SpanNotQueryBuilder
specifier|public
class|class
name|SpanNotQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|SpanNotQueryBuilder
argument_list|>
implements|implements
name|SpanQueryBuilder
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_not"
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
comment|/** the default pre parameter size */
DECL|field|DEFAULT_PRE
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_PRE
init|=
literal|0
decl_stmt|;
comment|/** the default post parameter size */
DECL|field|DEFAULT_POST
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_POST
init|=
literal|0
decl_stmt|;
DECL|field|POST_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|POST_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"post"
argument_list|)
decl_stmt|;
DECL|field|PRE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|PRE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"pre"
argument_list|)
decl_stmt|;
DECL|field|DIST_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|DIST_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"dist"
argument_list|)
decl_stmt|;
DECL|field|EXCLUDE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|EXCLUDE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"exclude"
argument_list|)
decl_stmt|;
DECL|field|INCLUDE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|INCLUDE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"include"
argument_list|)
decl_stmt|;
DECL|field|include
specifier|private
specifier|final
name|SpanQueryBuilder
name|include
decl_stmt|;
DECL|field|exclude
specifier|private
specifier|final
name|SpanQueryBuilder
name|exclude
decl_stmt|;
DECL|field|pre
specifier|private
name|int
name|pre
init|=
name|DEFAULT_PRE
decl_stmt|;
DECL|field|post
specifier|private
name|int
name|post
init|=
name|DEFAULT_POST
decl_stmt|;
comment|/**      * Construct a span query matching spans from<code>include</code> which      * have no overlap with spans from<code>exclude</code>.      * @param include the span query whose matches are filtered      * @param exclude the span query whose matches must not overlap      */
DECL|method|SpanNotQueryBuilder
specifier|public
name|SpanNotQueryBuilder
parameter_list|(
name|SpanQueryBuilder
name|include
parameter_list|,
name|SpanQueryBuilder
name|exclude
parameter_list|)
block|{
if|if
condition|(
name|include
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner clause [include] cannot be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|exclude
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner clause [exclude] cannot be null."
argument_list|)
throw|;
block|}
name|this
operator|.
name|include
operator|=
name|include
expr_stmt|;
name|this
operator|.
name|exclude
operator|=
name|exclude
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|SpanNotQueryBuilder
specifier|public
name|SpanNotQueryBuilder
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
name|include
operator|=
operator|(
name|SpanQueryBuilder
operator|)
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|)
expr_stmt|;
name|exclude
operator|=
operator|(
name|SpanQueryBuilder
operator|)
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|)
expr_stmt|;
name|pre
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|post
operator|=
name|in
operator|.
name|readVInt
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
name|writeNamedWriteable
argument_list|(
name|include
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|exclude
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|pre
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|post
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return the span query whose matches are filtered      */
DECL|method|includeQuery
specifier|public
name|SpanQueryBuilder
name|includeQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|include
return|;
block|}
comment|/**      * @return the span query whose matches must not overlap      */
DECL|method|excludeQuery
specifier|public
name|SpanQueryBuilder
name|excludeQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|exclude
return|;
block|}
comment|/**      * @param dist the amount of tokens from within the include span canât have overlap with the exclude span.      * Equivalent to setting both pre and post parameter.      */
DECL|method|dist
specifier|public
name|SpanNotQueryBuilder
name|dist
parameter_list|(
name|int
name|dist
parameter_list|)
block|{
name|pre
argument_list|(
name|dist
argument_list|)
expr_stmt|;
name|post
argument_list|(
name|dist
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @param pre the amount of tokens before the include span that canât have overlap with the exclude span. Values      * smaller than 0 will be ignored and 0 used instead.      */
DECL|method|pre
specifier|public
name|SpanNotQueryBuilder
name|pre
parameter_list|(
name|int
name|pre
parameter_list|)
block|{
name|this
operator|.
name|pre
operator|=
operator|(
name|pre
operator|>=
literal|0
operator|)
condition|?
name|pre
else|:
literal|0
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the amount of tokens before the include span that canât have overlap with the exclude span.      * @see SpanNotQueryBuilder#pre(int)      */
DECL|method|pre
specifier|public
name|Integer
name|pre
parameter_list|()
block|{
return|return
name|this
operator|.
name|pre
return|;
block|}
comment|/**      * @param post the amount of tokens after the include span that canât have overlap with the exclude span.      */
DECL|method|post
specifier|public
name|SpanNotQueryBuilder
name|post
parameter_list|(
name|int
name|post
parameter_list|)
block|{
name|this
operator|.
name|post
operator|=
operator|(
name|post
operator|>=
literal|0
operator|)
condition|?
name|post
else|:
literal|0
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the amount of tokens after the include span that canât have overlap with the exclude span.      * @see SpanNotQueryBuilder#post(int)      */
DECL|method|post
specifier|public
name|Integer
name|post
parameter_list|()
block|{
return|return
name|this
operator|.
name|post
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
name|field
argument_list|(
name|INCLUDE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|include
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|EXCLUDE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|exclude
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|PRE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|pre
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|POST_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|post
argument_list|)
expr_stmt|;
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
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|Optional
argument_list|<
name|SpanNotQueryBuilder
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
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|SpanQueryBuilder
name|include
init|=
literal|null
decl_stmt|;
name|SpanQueryBuilder
name|exclude
init|=
literal|null
decl_stmt|;
name|Integer
name|dist
init|=
literal|null
decl_stmt|;
name|Integer
name|pre
init|=
literal|null
decl_stmt|;
name|Integer
name|post
init|=
literal|null
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
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
name|INCLUDE_FIELD
argument_list|)
condition|)
block|{
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|query
init|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|.
name|isPresent
argument_list|()
operator|==
literal|false
operator|||
name|query
operator|.
name|get
argument_list|()
operator|instanceof
name|SpanQueryBuilder
operator|==
literal|false
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
literal|"spanNot [include] must be of type span query"
argument_list|)
throw|;
block|}
name|include
operator|=
operator|(
name|SpanQueryBuilder
operator|)
name|query
operator|.
name|get
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
name|EXCLUDE_FIELD
argument_list|)
condition|)
block|{
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|query
init|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|.
name|isPresent
argument_list|()
operator|==
literal|false
operator|||
name|query
operator|.
name|get
argument_list|()
operator|instanceof
name|SpanQueryBuilder
operator|==
literal|false
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
literal|"spanNot [exclude] must be of type span query"
argument_list|)
throw|;
block|}
name|exclude
operator|=
operator|(
name|SpanQueryBuilder
operator|)
name|query
operator|.
name|get
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
literal|"[span_not] query does not support ["
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
name|DIST_FIELD
argument_list|)
condition|)
block|{
name|dist
operator|=
name|parser
operator|.
name|intValue
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
name|PRE_FIELD
argument_list|)
condition|)
block|{
name|pre
operator|=
name|parser
operator|.
name|intValue
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
name|POST_FIELD
argument_list|)
condition|)
block|{
name|post
operator|=
name|parser
operator|.
name|intValue
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
literal|"[span_not] query does not support ["
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
name|include
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
literal|"spanNot must have [include] span query clause"
argument_list|)
throw|;
block|}
if|if
condition|(
name|exclude
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
literal|"spanNot must have [exclude] span query clause"
argument_list|)
throw|;
block|}
if|if
condition|(
name|dist
operator|!=
literal|null
operator|&&
operator|(
name|pre
operator|!=
literal|null
operator|||
name|post
operator|!=
literal|null
operator|)
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
literal|"spanNot can either use [dist] or [pre]& [post] (or none)"
argument_list|)
throw|;
block|}
name|SpanNotQueryBuilder
name|spanNotQuery
init|=
operator|new
name|SpanNotQueryBuilder
argument_list|(
name|include
argument_list|,
name|exclude
argument_list|)
decl_stmt|;
if|if
condition|(
name|dist
operator|!=
literal|null
condition|)
block|{
name|spanNotQuery
operator|.
name|dist
argument_list|(
name|dist
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pre
operator|!=
literal|null
condition|)
block|{
name|spanNotQuery
operator|.
name|pre
argument_list|(
name|pre
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|post
operator|!=
literal|null
condition|)
block|{
name|spanNotQuery
operator|.
name|post
argument_list|(
name|post
argument_list|)
expr_stmt|;
block|}
name|spanNotQuery
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|spanNotQuery
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|of
argument_list|(
name|spanNotQuery
argument_list|)
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
name|Query
name|includeQuery
init|=
name|this
operator|.
name|include
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
assert|assert
name|includeQuery
operator|instanceof
name|SpanQuery
assert|;
name|Query
name|excludeQuery
init|=
name|this
operator|.
name|exclude
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
assert|assert
name|excludeQuery
operator|instanceof
name|SpanQuery
assert|;
return|return
operator|new
name|SpanNotQuery
argument_list|(
operator|(
name|SpanQuery
operator|)
name|includeQuery
argument_list|,
operator|(
name|SpanQuery
operator|)
name|excludeQuery
argument_list|,
name|pre
argument_list|,
name|post
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
name|include
argument_list|,
name|exclude
argument_list|,
name|pre
argument_list|,
name|post
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
name|SpanNotQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|include
argument_list|,
name|other
operator|.
name|include
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|exclude
argument_list|,
name|other
operator|.
name|exclude
argument_list|)
operator|&&
operator|(
name|pre
operator|==
name|other
operator|.
name|pre
operator|)
operator|&&
operator|(
name|post
operator|==
name|other
operator|.
name|post
operator|)
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
block|}
end_class

end_unit

