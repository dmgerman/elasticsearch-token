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
name|compute
operator|.
name|ComputeCredential
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
name|javanet
operator|.
name|NetHttpTransport
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
name|JsonFactory
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
name|jackson2
operator|.
name|JacksonFactory
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
name|compute
operator|.
name|Compute
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
name|compute
operator|.
name|model
operator|.
name|Instance
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
name|compute
operator|.
name|model
operator|.
name|InstanceList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|Setting
operator|.
name|Property
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|gce
operator|.
name|RetryHttpInitializerWrapper
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
name|URL
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
name|PrivilegedActionException
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
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
DECL|class|GceComputeServiceImpl
specifier|public
class|class
name|GceComputeServiceImpl
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|GceComputeService
argument_list|>
implements|implements
name|GceComputeService
block|{
comment|// all settings just used for testing - not registered by default
DECL|field|GCE_VALIDATE_CERTIFICATES
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|GCE_VALIDATE_CERTIFICATES
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"cloud.gce.validate_certificates"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
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
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|GCE_ROOT_URL
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|GCE_ROOT_URL
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.gce.root_url"
argument_list|,
literal|"https://www.googleapis.com"
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|project
specifier|private
specifier|final
name|String
name|project
decl_stmt|;
DECL|field|zones
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|zones
decl_stmt|;
comment|// Forcing Google Token API URL as set in GCE SDK to
comment|//      http://metadata/computeMetadata/v1/instance/service-accounts/default/token
comment|// See https://developers.google.com/compute/docs/metadata#metadataserver
DECL|field|gceHost
specifier|private
specifier|final
name|String
name|gceHost
decl_stmt|;
DECL|field|metaDataUrl
specifier|private
specifier|final
name|String
name|metaDataUrl
decl_stmt|;
DECL|field|tokenServerEncodedUrl
specifier|private
specifier|final
name|String
name|tokenServerEncodedUrl
decl_stmt|;
DECL|field|gceRootUrl
specifier|private
name|String
name|gceRootUrl
decl_stmt|;
annotation|@
name|Override
DECL|method|instances
specifier|public
name|Collection
argument_list|<
name|Instance
argument_list|>
name|instances
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"get instances for project [{}], zones [{}]"
argument_list|,
name|project
argument_list|,
name|zones
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|Instance
argument_list|>
name|instances
init|=
name|zones
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|zoneId
parameter_list|)
lambda|->
block|{
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
name|InstanceList
name|instanceList
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|InstanceList
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InstanceList
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Compute
operator|.
name|Instances
operator|.
name|List
name|list
init|=
name|client
argument_list|()
operator|.
name|instances
argument_list|()
operator|.
name|list
argument_list|(
name|project
argument_list|,
name|zoneId
argument_list|)
decl_stmt|;
return|return
name|list
operator|.
name|execute
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// assist type inference
return|return
name|instanceList
operator|.
name|isEmpty
argument_list|()
operator|||
name|instanceList
operator|.
name|getItems
argument_list|()
operator|==
literal|null
condition|?
name|Collections
operator|.
expr|<
name|Instance
operator|>
name|emptyList
argument_list|()
else|:
name|instanceList
operator|.
name|getItems
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|PrivilegedActionException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Problem fetching instance list for zone {}"
argument_list|,
name|e
argument_list|,
name|zoneId
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Full exception:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// assist type inference
return|return
name|Collections
operator|.
expr|<
name|Instance
operator|>
name|emptyList
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|reduce
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
parameter_list|(
name|a
parameter_list|,
name|b
parameter_list|)
lambda|->
block|{
name|a
operator|.
name|addAll
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|instances
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"disabling GCE discovery. Can not get list of nodes"
argument_list|)
expr_stmt|;
block|}
return|return
name|instances
return|;
block|}
annotation|@
name|Override
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
block|{
name|String
name|urlMetadataNetwork
init|=
name|this
operator|.
name|metaDataUrl
operator|+
literal|"/"
operator|+
name|metadataPath
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
specifier|final
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|urlMetadataNetwork
argument_list|)
decl_stmt|;
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
name|url
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
DECL|field|client
specifier|private
name|Compute
name|client
decl_stmt|;
DECL|field|refreshInterval
specifier|private
name|TimeValue
name|refreshInterval
init|=
literal|null
decl_stmt|;
DECL|field|lastRefresh
specifier|private
name|long
name|lastRefresh
decl_stmt|;
comment|/** Global instance of the HTTP transport. */
DECL|field|gceHttpTransport
specifier|private
name|HttpTransport
name|gceHttpTransport
decl_stmt|;
comment|/** Global instance of the JSON factory. */
DECL|field|gceJsonFactory
specifier|private
name|JsonFactory
name|gceJsonFactory
decl_stmt|;
DECL|field|validateCerts
specifier|private
specifier|final
name|boolean
name|validateCerts
decl_stmt|;
annotation|@
name|Inject
DECL|method|GceComputeServiceImpl
specifier|public
name|GceComputeServiceImpl
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
name|this
operator|.
name|project
operator|=
name|PROJECT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|zones
operator|=
name|ZONE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|gceHost
operator|=
name|GCE_HOST
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaDataUrl
operator|=
name|gceHost
operator|+
literal|"/computeMetadata/v1/instance"
expr_stmt|;
name|this
operator|.
name|gceRootUrl
operator|=
name|GCE_ROOT_URL
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|tokenServerEncodedUrl
operator|=
name|metaDataUrl
operator|+
literal|"/service-accounts/default/token"
expr_stmt|;
name|this
operator|.
name|validateCerts
operator|=
name|GCE_VALIDATE_CERTIFICATES
operator|.
name|get
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
if|if
condition|(
name|validateCerts
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
else|else
block|{
comment|// this is only used for testing - alternative we could use the defaul keystore but this requires special configs too..
name|gceHttpTransport
operator|=
operator|new
name|NetHttpTransport
operator|.
name|Builder
argument_list|()
operator|.
name|doNotValidateCertificate
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|gceHttpTransport
return|;
block|}
DECL|method|client
specifier|public
specifier|synchronized
name|Compute
name|client
parameter_list|()
block|{
if|if
condition|(
name|refreshInterval
operator|!=
literal|null
operator|&&
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|client
operator|!=
literal|null
operator|&&
operator|(
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|<
literal|0
operator|||
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastRefresh
operator|)
operator|<
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|logger
operator|.
name|trace
argument_list|(
literal|"using cache to retrieve client"
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
name|lastRefresh
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|gceJsonFactory
operator|=
operator|new
name|JacksonFactory
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"starting GCE discovery service"
argument_list|)
expr_stmt|;
name|ComputeCredential
name|credential
init|=
operator|new
name|ComputeCredential
operator|.
name|Builder
argument_list|(
name|getGceHttpTransport
argument_list|()
argument_list|,
name|gceJsonFactory
argument_list|)
operator|.
name|setTokenServerEncodedUrl
argument_list|(
name|this
operator|.
name|tokenServerEncodedUrl
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|credential
operator|.
name|refreshToken
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"token [{}] will expire in [{}] s"
argument_list|,
name|credential
operator|.
name|getAccessToken
argument_list|()
argument_list|,
name|credential
operator|.
name|getExpiresInSeconds
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|credential
operator|.
name|getExpiresInSeconds
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|refreshInterval
operator|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
name|credential
operator|.
name|getExpiresInSeconds
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Compute
operator|.
name|Builder
name|builder
init|=
operator|new
name|Compute
operator|.
name|Builder
argument_list|(
name|getGceHttpTransport
argument_list|()
argument_list|,
name|gceJsonFactory
argument_list|,
literal|null
argument_list|)
operator|.
name|setApplicationName
argument_list|(
name|VERSION
argument_list|)
operator|.
name|setRootUrl
argument_list|(
name|gceRootUrl
argument_list|)
decl_stmt|;
if|if
condition|(
name|RETRY_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|TimeValue
name|maxWait
init|=
name|MAX_WAIT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|RetryHttpInitializerWrapper
name|retryHttpInitializerWrapper
decl_stmt|;
if|if
condition|(
name|maxWait
operator|.
name|getMillis
argument_list|()
operator|>
literal|0
condition|)
block|{
name|retryHttpInitializerWrapper
operator|=
operator|new
name|RetryHttpInitializerWrapper
argument_list|(
name|credential
argument_list|,
name|maxWait
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|retryHttpInitializerWrapper
operator|=
operator|new
name|RetryHttpInitializerWrapper
argument_list|(
name|credential
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setHttpRequestInitializer
argument_list|(
name|retryHttpInitializerWrapper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|setHttpRequestInitializer
argument_list|(
name|credential
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|client
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unable to start GCE discovery service"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unable to start GCE discovery service"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|client
return|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
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
throws|throws
name|ElasticsearchException
block|{     }
block|}
end_class

end_unit

