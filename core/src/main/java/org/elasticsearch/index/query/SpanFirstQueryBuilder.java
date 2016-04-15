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
name|SpanFirstQuery
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

begin_class
DECL|class|SpanFirstQueryBuilder
specifier|public
class|class
name|SpanFirstQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|SpanFirstQueryBuilder
argument_list|>
implements|implements
name|SpanQueryBuilder
argument_list|<
name|SpanFirstQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_first"
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
DECL|field|MATCH_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|MATCH_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"match"
argument_list|)
decl_stmt|;
DECL|field|END_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|END_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"end"
argument_list|)
decl_stmt|;
DECL|field|matchBuilder
specifier|private
specifier|final
name|SpanQueryBuilder
argument_list|<
name|?
argument_list|>
name|matchBuilder
decl_stmt|;
DECL|field|end
specifier|private
specifier|final
name|int
name|end
decl_stmt|;
comment|/**      * Query that matches spans queries defined in<code>matchBuilder</code>      * whose end position is less than or equal to<code>end</code>.      * @param matchBuilder inner {@link SpanQueryBuilder}      * @param end maximum end position of the match, needs to be positive      * @throws IllegalArgumentException for negative<code>end</code> positions      */
DECL|method|SpanFirstQueryBuilder
specifier|public
name|SpanFirstQueryBuilder
parameter_list|(
name|SpanQueryBuilder
argument_list|<
name|?
argument_list|>
name|matchBuilder
parameter_list|,
name|int
name|end
parameter_list|)
block|{
if|if
condition|(
name|matchBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner span query cannot be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|end
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"parameter [end] needs to be positive."
argument_list|)
throw|;
block|}
name|this
operator|.
name|matchBuilder
operator|=
name|matchBuilder
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|SpanFirstQueryBuilder
specifier|public
name|SpanFirstQueryBuilder
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
name|matchBuilder
operator|=
operator|(
name|SpanQueryBuilder
argument_list|<
name|?
argument_list|>
operator|)
name|in
operator|.
name|readQuery
argument_list|()
expr_stmt|;
name|end
operator|=
name|in
operator|.
name|readInt
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
name|writeQuery
argument_list|(
name|matchBuilder
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|end
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return the inner {@link SpanQueryBuilder} defined in this query      */
DECL|method|innerQuery
specifier|public
name|SpanQueryBuilder
argument_list|<
name|?
argument_list|>
name|innerQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|matchBuilder
return|;
block|}
comment|/**      * @return maximum end position of the matching inner span query      */
DECL|method|end
specifier|public
name|int
name|end
parameter_list|()
block|{
return|return
name|this
operator|.
name|end
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
name|MATCH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|matchBuilder
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
name|END_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|end
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
name|SpanFirstQueryBuilder
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
name|match
init|=
literal|null
decl_stmt|;
name|Integer
name|end
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
name|MATCH_FIELD
argument_list|)
condition|)
block|{
name|QueryBuilder
name|query
init|=
name|parseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|query
operator|instanceof
name|SpanQueryBuilder
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
literal|"spanFirst [match] must be of type span query"
argument_list|)
throw|;
block|}
name|match
operator|=
operator|(
name|SpanQueryBuilder
operator|)
name|query
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
literal|"[span_first] query does not support ["
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
name|END_FIELD
argument_list|)
condition|)
block|{
name|end
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
literal|"[span_first] query does not support ["
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
name|match
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
literal|"spanFirst must have [match] span query clause"
argument_list|)
throw|;
block|}
if|if
condition|(
name|end
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
literal|"spanFirst must have [end] set for it"
argument_list|)
throw|;
block|}
name|SpanFirstQueryBuilder
name|queryBuilder
init|=
operator|new
name|SpanFirstQueryBuilder
argument_list|(
name|match
argument_list|,
name|end
argument_list|)
decl_stmt|;
name|queryBuilder
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
expr_stmt|;
return|return
name|queryBuilder
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
name|innerSpanQuery
init|=
name|matchBuilder
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
assert|assert
name|innerSpanQuery
operator|instanceof
name|SpanQuery
assert|;
return|return
operator|new
name|SpanFirstQuery
argument_list|(
operator|(
name|SpanQuery
operator|)
name|innerSpanQuery
argument_list|,
name|end
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
name|matchBuilder
argument_list|,
name|end
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
name|SpanFirstQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|matchBuilder
argument_list|,
name|other
operator|.
name|matchBuilder
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|end
argument_list|,
name|other
operator|.
name|end
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
block|}
end_class

end_unit

