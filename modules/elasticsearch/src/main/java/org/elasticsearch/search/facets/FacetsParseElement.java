begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Lists
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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonToken
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
name|json
operator|.
name|JsonIndexQueryParser
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
name|SearchParseElement
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
name|SearchParseException
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|Booleans
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

begin_comment
comment|/**  *<pre>  * facets : {  *  query_execution : "collect|idset",  *  facet1: {  *      query : { ... },  *      global : false  *  }  * }  *</pre>  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FacetsParseElement
specifier|public
class|class
name|FacetsParseElement
implements|implements
name|SearchParseElement
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|void
name|parse
parameter_list|(
name|JsonParser
name|jp
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|JsonToken
name|token
decl_stmt|;
name|SearchContextFacets
operator|.
name|QueryExecutionType
name|queryExecutionType
init|=
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|COLLECT
decl_stmt|;
name|List
argument_list|<
name|SearchContextFacets
operator|.
name|QueryFacet
argument_list|>
name|queryFacets
init|=
literal|null
decl_stmt|;
name|String
name|topLevelFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|topLevelFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"query_execution"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
operator|||
literal|"queryExecution"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|String
name|text
init|=
name|jp
operator|.
name|getText
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"collect"
operator|.
name|equals
argument_list|(
name|text
argument_list|)
condition|)
block|{
name|queryExecutionType
operator|=
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|COLLECT
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"idset"
operator|.
name|equals
argument_list|(
name|text
argument_list|)
condition|)
block|{
name|queryExecutionType
operator|=
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|IDSET
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unsupported query type ["
operator|+
name|text
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
name|SearchContextFacets
operator|.
name|Facet
name|facet
init|=
literal|null
decl_stmt|;
name|boolean
name|global
init|=
literal|false
decl_stmt|;
name|String
name|facetFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|facetFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
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
name|facetFieldName
argument_list|)
condition|)
block|{
name|JsonIndexQueryParser
name|indexQueryParser
init|=
operator|(
name|JsonIndexQueryParser
operator|)
name|context
operator|.
name|queryParser
argument_list|()
decl_stmt|;
name|Query
name|facetQuery
init|=
name|indexQueryParser
operator|.
name|parse
argument_list|(
name|jp
argument_list|)
decl_stmt|;
name|facet
operator|=
operator|new
name|SearchContextFacets
operator|.
name|QueryFacet
argument_list|(
name|topLevelFieldName
argument_list|,
name|facetQuery
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryFacets
operator|==
literal|null
condition|)
block|{
name|queryFacets
operator|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|queryFacets
operator|.
name|add
argument_list|(
operator|(
name|SearchContextFacets
operator|.
name|QueryFacet
operator|)
name|facet
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_TRUE
condition|)
block|{
if|if
condition|(
literal|"global"
operator|.
name|equals
argument_list|(
name|facetFieldName
argument_list|)
condition|)
block|{
name|global
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NUMBER_INT
condition|)
block|{
name|global
operator|=
name|jp
operator|.
name|getIntValue
argument_list|()
operator|!=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
name|global
operator|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|,
name|global
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|facet
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"No facet type found for ["
operator|+
name|topLevelFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|facet
operator|.
name|global
argument_list|(
name|global
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|queryExecutionType
operator|==
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|IDSET
condition|)
block|{
comment|// if we are using doc id sets, we need to enable the fact that we accumelate it
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|enabledDocIdSet
argument_list|()
expr_stmt|;
block|}
name|context
operator|.
name|facets
argument_list|(
operator|new
name|SearchContextFacets
argument_list|(
name|queryExecutionType
argument_list|,
name|queryFacets
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

