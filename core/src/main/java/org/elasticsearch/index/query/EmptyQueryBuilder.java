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
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
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
name|XContentType
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
comment|/**  * A {@link QueryBuilder} that is a stand in replacement for an empty query clause in the DSL.  * The current DSL allows parsing inner queries / filters like "{ }", in order to have a  * valid non-null representation of these clauses that actually do nothing we can use this class.  *  * This builder has no corresponding parser and it is not registered under the query name. It is  * intended to be used internally as a stand-in for nested queries that are left empty and should  * be ignored upstream.  */
end_comment

begin_class
DECL|class|EmptyQueryBuilder
specifier|public
class|class
name|EmptyQueryBuilder
extends|extends
name|ToXContentToBytes
implements|implements
name|QueryBuilder
argument_list|<
name|EmptyQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"empty_query"
decl_stmt|;
comment|/** the one and only empty query builder */
DECL|field|PROTOTYPE
specifier|public
specifier|static
specifier|final
name|EmptyQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|EmptyQueryBuilder
argument_list|()
decl_stmt|;
comment|// prevent instances other than prototype
DECL|method|EmptyQueryBuilder
specifier|private
name|EmptyQueryBuilder
parameter_list|()
block|{
name|super
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|getWriteableName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|toQuery
specifier|public
name|Query
name|toQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// empty
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|toFilter
specifier|public
name|Query
name|toFilter
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// empty
return|return
literal|null
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
block|{     }
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|EmptyQueryBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
annotation|@
name|Override
DECL|method|queryName
specifier|public
name|EmptyQueryBuilder
name|queryName
parameter_list|(
name|String
name|queryName
parameter_list|)
block|{
comment|//no-op
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|queryName
specifier|public
name|String
name|queryName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|EmptyQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
comment|//no-op
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

