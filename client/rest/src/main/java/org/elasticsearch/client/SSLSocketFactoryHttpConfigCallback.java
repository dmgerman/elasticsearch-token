begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|config
operator|.
name|Registry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|config
operator|.
name|RegistryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|conn
operator|.
name|socket
operator|.
name|ConnectionSocketFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|conn
operator|.
name|socket
operator|.
name|PlainConnectionSocketFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|conn
operator|.
name|ssl
operator|.
name|SSLConnectionSocketFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|HttpClientBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|conn
operator|.
name|PoolingHttpClientConnectionManager
import|;
end_import

begin_comment
comment|/**  * Helps configuring the http client when needing to communicate over ssl. It effectively replaces the connection manager  * with one that has ssl properly configured thanks to the provided {@link SSLConnectionSocketFactory}.  */
end_comment

begin_class
DECL|class|SSLSocketFactoryHttpConfigCallback
specifier|public
class|class
name|SSLSocketFactoryHttpConfigCallback
implements|implements
name|RestClient
operator|.
name|HttpClientConfigCallback
block|{
DECL|field|sslSocketFactory
specifier|private
specifier|final
name|SSLConnectionSocketFactory
name|sslSocketFactory
decl_stmt|;
DECL|method|SSLSocketFactoryHttpConfigCallback
specifier|public
name|SSLSocketFactoryHttpConfigCallback
parameter_list|(
name|SSLConnectionSocketFactory
name|sslSocketFactory
parameter_list|)
block|{
name|this
operator|.
name|sslSocketFactory
operator|=
name|sslSocketFactory
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|customizeHttpClient
specifier|public
name|void
name|customizeHttpClient
parameter_list|(
name|HttpClientBuilder
name|httpClientBuilder
parameter_list|)
block|{
name|Registry
argument_list|<
name|ConnectionSocketFactory
argument_list|>
name|socketFactoryRegistry
init|=
name|RegistryBuilder
operator|.
expr|<
name|ConnectionSocketFactory
operator|>
name|create
argument_list|()
operator|.
name|register
argument_list|(
literal|"http"
argument_list|,
name|PlainConnectionSocketFactory
operator|.
name|getSocketFactory
argument_list|()
argument_list|)
operator|.
name|register
argument_list|(
literal|"https"
argument_list|,
name|sslSocketFactory
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|PoolingHttpClientConnectionManager
name|connectionManager
init|=
operator|new
name|PoolingHttpClientConnectionManager
argument_list|(
name|socketFactoryRegistry
argument_list|)
decl_stmt|;
comment|//default settings may be too constraining
name|connectionManager
operator|.
name|setDefaultMaxPerRoute
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|connectionManager
operator|.
name|setMaxTotal
argument_list|(
literal|30
argument_list|)
expr_stmt|;
name|httpClientBuilder
operator|.
name|setConnectionManager
argument_list|(
name|connectionManager
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

