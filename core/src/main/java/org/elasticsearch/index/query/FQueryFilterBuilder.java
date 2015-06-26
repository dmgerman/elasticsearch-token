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

begin_comment
comment|/**  * A filter that simply wraps a query. Same as the {@link QueryFilterBuilder} except that it allows also to  * associate a name with the query filter.  * @deprecated Useless now that queries and filters are merged: pass the  *             query as a filter directly.  */
end_comment

begin_class
annotation|@
name|Deprecated
DECL|class|FQueryFilterBuilder
specifier|public
class|class
name|FQueryFilterBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|FQueryFilterBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"fquery"
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|FQueryFilterBuilder
name|PROTOTYPE
init|=
operator|new
name|FQueryFilterBuilder
argument_list|(
literal|null
argument_list|)
decl_stmt|;
DECL|field|queryName
specifier|private
name|String
name|queryName
decl_stmt|;
DECL|field|queryBuilder
specifier|private
specifier|final
name|QueryBuilder
name|queryBuilder
decl_stmt|;
comment|/**      * A filter that simply wraps a query.      *      * @param queryBuilder The query to wrap as a filter      */
DECL|method|FQueryFilterBuilder
specifier|public
name|FQueryFilterBuilder
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
block|}
comment|/**      * @return the query builder that is wrapped by this {@link FQueryFilterBuilder}      */
DECL|method|innerQuery
specifier|public
name|QueryBuilder
name|innerQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryBuilder
return|;
block|}
comment|/**      * Sets the query name for the filter that can be used when searching for matched_filters per hit.      */
DECL|method|queryName
specifier|public
name|FQueryFilterBuilder
name|queryName
parameter_list|(
name|String
name|queryName
parameter_list|)
block|{
name|this
operator|.
name|queryName
operator|=
name|queryName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the query name for the filter that can be used when searching for matched_filters per hit      */
DECL|method|queryName
specifier|public
name|String
name|queryName
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryName
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
name|FQueryFilterBuilder
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|doXContentInnerBuilder
argument_list|(
name|builder
argument_list|,
literal|"query"
argument_list|,
name|queryBuilder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_name"
argument_list|,
name|queryName
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toQuery
specifier|public
name|Query
name|toQuery
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|QueryParsingException
throws|,
name|IOException
block|{
comment|// inner query builder can potentially be `null`, in that case we ignore it
if|if
condition|(
name|this
operator|.
name|queryBuilder
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Query
name|innerQuery
init|=
name|this
operator|.
name|queryBuilder
operator|.
name|toQuery
argument_list|(
name|parseContext
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
name|Query
name|query
init|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|innerQuery
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
annotation|@
name|Override
DECL|method|validate
specifier|public
name|QueryValidationException
name|validate
parameter_list|()
block|{
comment|// nothing to validate
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|queryBuilder
argument_list|,
name|queryName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|public
name|boolean
name|doEquals
parameter_list|(
name|FQueryFilterBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|queryBuilder
argument_list|,
name|other
operator|.
name|queryBuilder
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|queryName
argument_list|,
name|other
operator|.
name|queryName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|FQueryFilterBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
name|innerQueryBuilder
init|=
name|in
operator|.
name|readNamedWriteable
argument_list|()
decl_stmt|;
name|FQueryFilterBuilder
name|fquery
init|=
operator|new
name|FQueryFilterBuilder
argument_list|(
name|innerQueryBuilder
argument_list|)
decl_stmt|;
name|fquery
operator|.
name|queryName
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
return|return
name|fquery
return|;
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
name|writeNamedWriteable
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

