begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
package|;
end_package

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
name|Binder
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
name|ExtensionPoint
import|;
end_import

begin_comment
comment|/**  * A mapping from type name to implementations of {@link Repository}.  */
end_comment

begin_class
DECL|class|RepositoryTypesRegistry
specifier|public
class|class
name|RepositoryTypesRegistry
block|{
comment|// invariant: repositories and shardRepositories have the same keyset
DECL|field|repositoryTypes
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|SelectedType
argument_list|<
name|Repository
argument_list|>
name|repositoryTypes
init|=
operator|new
name|ExtensionPoint
operator|.
name|SelectedType
argument_list|<>
argument_list|(
literal|"repository"
argument_list|,
name|Repository
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Adds a new repository type to the registry, bound to the given implementation classes. */
DECL|method|registerRepository
specifier|public
name|void
name|registerRepository
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Repository
argument_list|>
name|repositoryType
parameter_list|)
block|{
name|repositoryTypes
operator|.
name|registerExtension
argument_list|(
name|name
argument_list|,
name|repositoryType
argument_list|)
expr_stmt|;
block|}
comment|/**      * Looks up the given type and binds the implementation into the given binder.      * Throws an {@link IllegalArgumentException} if the given type does not exist.      */
DECL|method|bindType
specifier|public
name|void
name|bindType
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
name|type
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|repositoryTypes
operator|.
name|bindType
argument_list|(
name|binder
argument_list|,
name|settings
argument_list|,
literal|"type"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

