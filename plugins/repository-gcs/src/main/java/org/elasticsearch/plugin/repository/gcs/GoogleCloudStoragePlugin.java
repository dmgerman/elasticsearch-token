begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.repository.gcs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|repository
operator|.
name|gcs
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|auth
operator|.
name|oauth2
operator|.
name|TokenRequest
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|auth
operator|.
name|oauth2
operator|.
name|TokenResponse
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|googleapis
operator|.
name|json
operator|.
name|GoogleJsonError
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|http
operator|.
name|GenericUrl
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|http
operator|.
name|HttpHeaders
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|json
operator|.
name|GenericJson
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|json
operator|.
name|webtoken
operator|.
name|JsonWebSignature
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|json
operator|.
name|webtoken
operator|.
name|JsonWebToken
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|util
operator|.
name|ClassInfo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|util
operator|.
name|Data
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|storage
operator|.
name|Storage
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|storage
operator|.
name|model
operator|.
name|Bucket
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|storage
operator|.
name|model
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|storage
operator|.
name|model
operator|.
name|StorageObject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
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
name|Module
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
name|snapshots
operator|.
name|blobstore
operator|.
name|BlobStoreIndexShardRepository
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|RepositoriesModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|gcs
operator|.
name|GoogleCloudStorageRepository
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_class
DECL|class|GoogleCloudStoragePlugin
specifier|public
class|class
name|GoogleCloudStoragePlugin
extends|extends
name|Plugin
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"repository-gcs"
decl_stmt|;
static|static
block|{
comment|/*          * Google HTTP client changes access levels because its silly and we          * can't allow that on any old stack stack so we pull it here, up front,          * so we can cleanly check the permissions for it. Without this changing          * the permission can fail if any part of core is on the stack because          * our plugin permissions don't allow core to "reach through" plugins to          * change the permission. Because that'd be silly.          */
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|AccessController
operator|.
name|doPrivileged
argument_list|(
call|(
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
call|)
argument_list|()
operator|->
block|{
comment|// ClassInfo put in cache all the fields of a given class
comment|// that are annoted with @Key; at the same time it changes
comment|// the field access level using setAccessible(). Calling
comment|// them here put the ClassInfo in cache (they are never evicted)
comment|// before the SecurityManager is installed.
name|ClassInfo
operator|.
name|of
argument_list|(
name|HttpHeaders
operator|.
name|class
argument_list|,
literal|true
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|JsonWebSignature
operator|.
name|Header
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|JsonWebToken
operator|.
name|Payload
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|TokenRequest
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|TokenResponse
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|GenericJson
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|GenericUrl
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|Data
operator|.
name|nullOf
argument_list|(
name|GoogleJsonError
operator|.
name|ErrorInfo
operator|.
name|class
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|GoogleJsonError
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|Data
operator|.
name|nullOf
argument_list|(
name|Bucket
operator|.
name|Cors
operator|.
name|class
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Cors
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Lifecycle
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Logging
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Owner
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Versioning
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Bucket
operator|.
name|Website
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|StorageObject
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|StorageObject
operator|.
name|Owner
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Objects
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Buckets
operator|.
name|Get
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Buckets
operator|.
name|Insert
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Objects
operator|.
name|Get
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Objects
operator|.
name|Insert
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Objects
operator|.
name|Delete
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Objects
operator|.
name|Copy
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
name|ClassInfo
operator|.
name|of
argument_list|(
name|Storage
operator|.
name|Objects
operator|.
name|List
operator|.
name|class
argument_list|,
literal|false
argument_list|)
block|;
return|return
literal|null
return|;
block|}
block|)
empty_stmt|;
block|}
end_class

begin_function
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Google Cloud Storage Repository Plugin"
return|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|nodeModules
specifier|public
name|Collection
argument_list|<
name|Module
argument_list|>
name|nodeModules
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|GoogleCloudStorageModule
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|RepositoriesModule
name|repositoriesModule
parameter_list|)
block|{
name|repositoriesModule
operator|.
name|registerRepository
argument_list|(
name|GoogleCloudStorageRepository
operator|.
name|TYPE
argument_list|,
name|GoogleCloudStorageRepository
operator|.
name|class
argument_list|,
name|BlobStoreIndexShardRepository
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit
