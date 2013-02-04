begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ConstantScoreQuery
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
name|Filter
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
name|XBooleanFilter
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
name|XFilteredQuery
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
name|search
operator|.
name|child
operator|.
name|HasParentFilter
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
name|ParentQuery
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
name|List
import|;
end_import

begin_class
DECL|class|HasParentQueryParser
specifier|public
class|class
name|HasParentQueryParser
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
literal|"has_parent"
decl_stmt|;
annotation|@
name|Inject
DECL|method|HasParentQueryParser
specifier|public
name|HasParentQueryParser
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
name|Query
name|innerQuery
init|=
literal|null
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
name|parentType
init|=
literal|null
decl_stmt|;
name|boolean
name|score
init|=
literal|false
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
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// TODO handle `query` element before `type` element...
name|String
index|[]
name|origTypes
init|=
name|QueryParseContext
operator|.
name|setTypesWithPrevious
argument_list|(
name|parentType
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|String
index|[]
block|{
name|parentType
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|innerQuery
operator|=
name|parseContext
operator|.
name|parseInnerQuery
argument_list|()
expr_stmt|;
name|queryFound
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|QueryParseContext
operator|.
name|setTypes
argument_list|(
name|origTypes
argument_list|)
expr_stmt|;
block|}
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
literal|"[has_parent] query does not support ["
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
operator|||
literal|"parent_type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"parentType"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|parentType
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
literal|"_scope"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
literal|"the [_scope] support in [has_parent] query has been removed, use a filter as a facet_filter in the relevant global facet"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
literal|"score_type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"scoreType"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|scoreTypeValue
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"score"
operator|.
name|equals
argument_list|(
name|scoreTypeValue
argument_list|)
condition|)
block|{
name|score
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|scoreTypeValue
argument_list|)
condition|)
block|{
name|score
operator|=
literal|false
expr_stmt|;
block|}
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
literal|"[has_parent] query does not support ["
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
literal|"[parent] query requires 'query' field"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|parentType
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
literal|"[parent] query requires 'parent_type' field"
argument_list|)
throw|;
block|}
name|DocumentMapper
name|parentDocMapper
init|=
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDocMapper
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
literal|"[parent] query configured 'parent_type' ["
operator|+
name|parentType
operator|+
literal|"] is not a valid type"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|childTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
for|for
control|(
name|DocumentMapper
name|documentMapper
range|:
name|parseContext
operator|.
name|mapperService
argument_list|()
control|)
block|{
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|documentMapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentFieldMapper
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|parentDocMapper
operator|.
name|type
argument_list|()
operator|.
name|equals
argument_list|(
name|parentFieldMapper
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|childTypes
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Filter
name|childFilter
decl_stmt|;
if|if
condition|(
name|childTypes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|DocumentMapper
name|documentMapper
init|=
name|parseContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|childTypes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|childFilter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|documentMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|XBooleanFilter
name|childrenFilter
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|childType
range|:
name|childTypes
control|)
block|{
name|DocumentMapper
name|documentMapper
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
name|Filter
name|filter
init|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|documentMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|childrenFilter
operator|.
name|add
argument_list|(
name|filter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
name|childFilter
operator|=
name|childrenFilter
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
name|XFilteredQuery
argument_list|(
name|innerQuery
argument_list|,
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|parentDocMapper
operator|.
name|typeFilter
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
name|Query
name|query
decl_stmt|;
if|if
condition|(
name|score
condition|)
block|{
name|ParentQuery
name|parentQuery
init|=
operator|new
name|ParentQuery
argument_list|(
name|searchContext
argument_list|,
name|innerQuery
argument_list|,
name|parentType
argument_list|,
name|childTypes
argument_list|,
name|childFilter
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|addRewrite
argument_list|(
name|parentQuery
argument_list|)
expr_stmt|;
name|query
operator|=
name|parentQuery
expr_stmt|;
block|}
else|else
block|{
name|HasParentFilter
name|hasParentFilter
init|=
name|HasParentFilter
operator|.
name|create
argument_list|(
name|innerQuery
argument_list|,
name|parentType
argument_list|,
name|searchContext
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|addRewrite
argument_list|(
name|hasParentFilter
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|hasParentFilter
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

