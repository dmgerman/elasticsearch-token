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
name|FilteredQuery
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
name|join
operator|.
name|BitDocIdSetFilter
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
name|fielddata
operator|.
name|plain
operator|.
name|ParentChildIndexFieldData
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
name|DocumentMapper
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
name|ParentFieldMapper
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
name|XContentStructure
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
name|child
operator|.
name|CustomQueryWrappingFilter
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
name|child
operator|.
name|ScoreType
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
name|child
operator|.
name|TopChildrenQuery
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
DECL|class|TopChildrenQueryParser
specifier|public
class|class
name|TopChildrenQueryParser
extends|extends
name|BaseQueryParserTemp
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"top_children"
decl_stmt|;
annotation|@
name|Inject
DECL|method|TopChildrenQueryParser
specifier|public
name|TopChildrenQueryParser
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
name|boolean
name|queryFound
init|=
literal|false
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|String
name|childType
init|=
literal|null
decl_stmt|;
name|ScoreType
name|scoreType
init|=
name|ScoreType
operator|.
name|MAX
decl_stmt|;
name|int
name|factor
init|=
literal|5
decl_stmt|;
name|int
name|incrementalFactor
init|=
literal|2
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
name|XContentStructure
operator|.
name|InnerQuery
name|iq
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
comment|// Usually, the query would be parsed here, but the child
comment|// type may not have been extracted yet, so use the
comment|// XContentStructure.<type> facade to parse if available,
comment|// or delay parsing if not.
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
name|iq
operator|=
operator|new
name|XContentStructure
operator|.
name|InnerQuery
argument_list|(
name|parseContext
argument_list|,
name|childType
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|String
index|[]
block|{
name|childType
block|}
argument_list|)
expr_stmt|;
name|queryFound
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[top_children] query does not support ["
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
literal|"type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|childType
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
literal|"score"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|scoreType
operator|=
name|ScoreType
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
literal|"score_mode"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"scoreMode"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|scoreType
operator|=
name|ScoreType
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
literal|"factor"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|factor
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
literal|"incremental_factor"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"incrementalFactor"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|incrementalFactor
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
operator|.
name|index
argument_list|()
argument_list|,
literal|"[top_children] query does not support ["
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
operator|!
name|queryFound
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[top_children] requires 'query' field"
argument_list|)
throw|;
block|}
if|if
condition|(
name|childType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[top_children] requires 'type' field"
argument_list|)
throw|;
block|}
name|Query
name|innerQuery
init|=
name|iq
operator|.
name|asQuery
argument_list|(
name|childType
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerQuery
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|DocumentMapper
name|childDocMapper
init|=
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|childType
argument_list|)
decl_stmt|;
if|if
condition|(
name|childDocMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"No mapping for for type ["
operator|+
name|childType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|childDocMapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|parentFieldMapper
operator|.
name|active
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Type ["
operator|+
name|childType
operator|+
literal|"] does not have parent mapping"
argument_list|)
throw|;
block|}
name|String
name|parentType
init|=
name|childDocMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|type
argument_list|()
decl_stmt|;
name|BitDocIdSetFilter
name|nonNestedDocsFilter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|childDocMapper
operator|.
name|hasNestedObjects
argument_list|()
condition|)
block|{
name|nonNestedDocsFilter
operator|=
name|parseContext
operator|.
name|bitsetFilter
argument_list|(
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|innerQuery
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
comment|// wrap the query with type query
name|innerQuery
operator|=
operator|new
name|FilteredQuery
argument_list|(
name|innerQuery
argument_list|,
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|childDocMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
literal|null
argument_list|,
name|parseContext
operator|.
name|autoFilterCachePolicy
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
init|=
name|parseContext
operator|.
name|getForField
argument_list|(
name|parentFieldMapper
argument_list|)
decl_stmt|;
name|TopChildrenQuery
name|query
init|=
operator|new
name|TopChildrenQuery
argument_list|(
name|parentChildIndexFieldData
argument_list|,
name|innerQuery
argument_list|,
name|childType
argument_list|,
name|parentType
argument_list|,
name|scoreType
argument_list|,
name|factor
argument_list|,
name|incrementalFactor
argument_list|,
name|nonNestedDocsFilter
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
name|addNamedFilter
argument_list|(
name|queryName
argument_list|,
operator|new
name|CustomQueryWrappingFilter
argument_list|(
name|query
argument_list|)
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

