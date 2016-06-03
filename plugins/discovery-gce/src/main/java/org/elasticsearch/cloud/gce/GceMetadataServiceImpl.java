begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|gce
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
name|googleapis
operator|.
name|javanet
operator|.
name|GoogleNetHttpTransport
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
name|http
operator|.
name|HttpResponse
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
name|HttpTransport
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
name|cloud
operator|.
name|gce
operator|.
name|network
operator|.
name|GceNameResolver
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|network
operator|.
name|NetworkService
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
name|Setting
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|GeneralSecurityException
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
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|GceMetadataServiceImpl
specifier|public
class|class
name|GceMetadataServiceImpl
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|GceMetadataServiceImpl
argument_list|>
block|{
comment|// Forcing Google Token API URL as set in GCE SDK to
comment|//      http://metadata/computeMetadata/v1/instance/service-accounts/default/token
comment|// See https://developers.google.com/compute/docs/metadata#metadataserver
comment|// all settings just used for testing - not registered by default
DECL|field|GCE_HOST
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|GCE_HOST
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.gce.host"
argument_list|,
literal|"http://metadata.google.internal"
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** Global instance of the HTTP transport. */
DECL|field|gceHttpTransport
specifier|private
name|HttpTransport
name|gceHttpTransport
decl_stmt|;
annotation|@
name|Inject
DECL|method|GceMetadataServiceImpl
specifier|public
name|GceMetadataServiceImpl
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|networkService
operator|.
name|addCustomNameResolver
argument_list|(
operator|new
name|GceNameResolver
argument_list|(
name|settings
argument_list|,
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getGceHttpTransport
specifier|protected
specifier|synchronized
name|HttpTransport
name|getGceHttpTransport
parameter_list|()
throws|throws
name|GeneralSecurityException
throws|,
name|IOException
block|{
if|if
condition|(
name|gceHttpTransport
operator|==
literal|null
condition|)
block|{
name|gceHttpTransport
operator|=
name|GoogleNetHttpTransport
operator|.
name|newTrustedTransport
argument_list|()
expr_stmt|;
block|}
return|return
name|gceHttpTransport
return|;
block|}
DECL|method|metadata
specifier|public
name|String
name|metadata
parameter_list|(
name|String
name|metadataPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
comment|// Forcing Google Token API URL as set in GCE SDK to
comment|//      http://metadata/computeMetadata/v1/instance/service-accounts/default/token
comment|// See https://developers.google.com/compute/docs/metadata#metadataserver
specifier|final
name|URI
name|urlMetadataNetwork
init|=
operator|new
name|URI
argument_list|(
name|GCE_HOST
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"/computeMetadata/v1/instance/"
argument_list|)
operator|.
name|resolve
argument_list|(
name|metadataPath
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"get metadata from [{}]"
argument_list|,
name|urlMetadataNetwork
argument_list|)
expr_stmt|;
name|HttpHeaders
name|headers
decl_stmt|;
try|try
block|{
comment|// hack around code messiness in GCE code
comment|// TODO: get this fixed
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
name|headers
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|HttpHeaders
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|HttpHeaders
name|run
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|HttpHeaders
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|GenericUrl
name|genericUrl
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|GenericUrl
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GenericUrl
name|run
parameter_list|()
block|{
return|return
operator|new
name|GenericUrl
argument_list|(
name|urlMetadataNetwork
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// This is needed to query meta data: https://cloud.google.com/compute/docs/metadata
name|headers
operator|.
name|put
argument_list|(
literal|"Metadata-Flavor"
argument_list|,
literal|"Google"
argument_list|)
expr_stmt|;
name|HttpResponse
name|response
decl_stmt|;
name|response
operator|=
name|getGceHttpTransport
argument_list|()
operator|.
name|createRequestFactory
argument_list|()
operator|.
name|buildGetRequest
argument_list|(
name|genericUrl
argument_list|)
operator|.
name|setHeaders
argument_list|(
name|headers
argument_list|)
operator|.
name|execute
argument_list|()
expr_stmt|;
name|String
name|metadata
init|=
name|response
operator|.
name|parseAsString
argument_list|()
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"metadata found [{}]"
argument_list|,
name|metadata
argument_list|)
expr_stmt|;
return|return
name|metadata
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed to fetch metadata from ["
operator|+
name|urlMetadataNetwork
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{      }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
if|if
condition|(
name|gceHttpTransport
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|gceHttpTransport
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unable to shutdown GCE Http Transport"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|gceHttpTransport
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{      }
block|}
end_class

end_unit

