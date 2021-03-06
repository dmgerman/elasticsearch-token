begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.wildfly.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|wildfly
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
operator|.
name|TransportClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
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
name|SuppressForbidden
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
name|PathUtils
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
name|transport
operator|.
name|TransportAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|client
operator|.
name|PreBuiltTransportClient
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|enterprise
operator|.
name|inject
operator|.
name|Produces
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|Properties
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
DECL|class|TransportClientProducer
specifier|public
specifier|final
class|class
name|TransportClientProducer
block|{
annotation|@
name|Produces
DECL|method|createTransportClient
specifier|public
name|TransportClient
name|createTransportClient
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|elasticsearchProperties
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"elasticsearch.properties"
argument_list|)
decl_stmt|;
specifier|final
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
specifier|final
name|String
name|transportUri
decl_stmt|;
specifier|final
name|String
name|clusterName
decl_stmt|;
try|try
init|(
name|InputStream
name|is
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|getPath
argument_list|(
name|elasticsearchProperties
argument_list|)
argument_list|)
init|)
block|{
name|properties
operator|.
name|load
argument_list|(
name|is
argument_list|)
expr_stmt|;
name|transportUri
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
literal|"transport.uri"
argument_list|)
expr_stmt|;
name|clusterName
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
literal|"cluster.name"
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|lastColon
init|=
name|transportUri
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
specifier|final
name|String
name|host
init|=
name|transportUri
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|lastColon
argument_list|)
decl_stmt|;
specifier|final
name|int
name|port
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|transportUri
operator|.
name|substring
argument_list|(
name|lastColon
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
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
literal|"cluster.name"
argument_list|,
name|clusterName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|TransportClient
name|transportClient
init|=
operator|new
name|PreBuiltTransportClient
argument_list|(
name|settings
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|transportClient
operator|.
name|addTransportAddress
argument_list|(
operator|new
name|TransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
argument_list|,
name|port
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|transportClient
return|;
block|}
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"get path not configured in environment"
argument_list|)
DECL|method|getPath
specifier|private
name|Path
name|getPath
parameter_list|(
specifier|final
name|String
name|elasticsearchProperties
parameter_list|)
block|{
return|return
name|PathUtils
operator|.
name|get
argument_list|(
name|elasticsearchProperties
argument_list|)
return|;
block|}
block|}
end_class

end_unit

