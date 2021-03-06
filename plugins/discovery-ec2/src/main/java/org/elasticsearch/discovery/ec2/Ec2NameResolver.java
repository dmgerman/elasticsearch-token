begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.ec2
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|ec2
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
name|util
operator|.
name|IOUtils
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
name|component
operator|.
name|AbstractComponent
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
operator|.
name|CustomNameResolver
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
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLConnection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_comment
comment|/**  * Resolves certain ec2 related 'meta' hostnames into an actual hostname  * obtained from ec2 meta-data.  *<p>  * Valid config values for {@link Ec2HostnameType}s are -  *<ul>  *<li>_ec2_ - maps to privateIpv4</li>  *<li>_ec2:privateIp_ - maps to privateIpv4</li>  *<li>_ec2:privateIpv4_</li>  *<li>_ec2:privateDns_</li>  *<li>_ec2:publicIp_ - maps to publicIpv4</li>  *<li>_ec2:publicIpv4_</li>  *<li>_ec2:publicDns_</li>  *</ul>  *  * @author Paul_Loy (keteracel)  */
end_comment

begin_class
DECL|class|Ec2NameResolver
class|class
name|Ec2NameResolver
extends|extends
name|AbstractComponent
implements|implements
name|CustomNameResolver
block|{
comment|/**      * enum that can be added to over time with more meta-data types (such as ipv6 when this is available)      *      * @author Paul_Loy      */
DECL|enum|Ec2HostnameType
specifier|private
enum|enum
name|Ec2HostnameType
block|{
DECL|enum constant|PRIVATE_IPv4
name|PRIVATE_IPv4
argument_list|(
literal|"ec2:privateIpv4"
argument_list|,
literal|"local-ipv4"
argument_list|)
block|,
DECL|enum constant|PRIVATE_DNS
name|PRIVATE_DNS
argument_list|(
literal|"ec2:privateDns"
argument_list|,
literal|"local-hostname"
argument_list|)
block|,
DECL|enum constant|PUBLIC_IPv4
name|PUBLIC_IPv4
argument_list|(
literal|"ec2:publicIpv4"
argument_list|,
literal|"public-ipv4"
argument_list|)
block|,
DECL|enum constant|PUBLIC_DNS
name|PUBLIC_DNS
argument_list|(
literal|"ec2:publicDns"
argument_list|,
literal|"public-hostname"
argument_list|)
block|,
comment|// some less verbose defaults
DECL|enum constant|PUBLIC_IP
name|PUBLIC_IP
argument_list|(
literal|"ec2:publicIp"
argument_list|,
name|PUBLIC_IPv4
operator|.
name|ec2Name
argument_list|)
block|,
DECL|enum constant|PRIVATE_IP
name|PRIVATE_IP
argument_list|(
literal|"ec2:privateIp"
argument_list|,
name|PRIVATE_IPv4
operator|.
name|ec2Name
argument_list|)
block|,
DECL|enum constant|EC2
name|EC2
argument_list|(
literal|"ec2"
argument_list|,
name|PRIVATE_IPv4
operator|.
name|ec2Name
argument_list|)
block|;
DECL|field|configName
specifier|final
name|String
name|configName
decl_stmt|;
DECL|field|ec2Name
specifier|final
name|String
name|ec2Name
decl_stmt|;
DECL|method|Ec2HostnameType
name|Ec2HostnameType
parameter_list|(
name|String
name|configName
parameter_list|,
name|String
name|ec2Name
parameter_list|)
block|{
name|this
operator|.
name|configName
operator|=
name|configName
expr_stmt|;
name|this
operator|.
name|ec2Name
operator|=
name|ec2Name
expr_stmt|;
block|}
block|}
comment|/**      * Construct a {@link CustomNameResolver}.      */
DECL|method|Ec2NameResolver
name|Ec2NameResolver
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param type the ec2 hostname type to discover.      * @return the appropriate host resolved from ec2 meta-data, or null if it cannot be obtained.      * @see CustomNameResolver#resolveIfPossible(String)      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"We call getInputStream in doPrivileged and provide SocketPermission"
argument_list|)
DECL|method|resolve
specifier|public
name|InetAddress
index|[]
name|resolve
parameter_list|(
name|Ec2HostnameType
name|type
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
literal|null
decl_stmt|;
name|String
name|metadataUrl
init|=
name|AwsEc2ServiceImpl
operator|.
name|EC2_METADATA_URL
operator|+
name|type
operator|.
name|ec2Name
decl_stmt|;
try|try
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|metadataUrl
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"obtaining ec2 hostname from ec2 meta-data url {}"
argument_list|,
name|url
argument_list|)
expr_stmt|;
name|URLConnection
name|urlConnection
init|=
name|SocketAccess
operator|.
name|doPrivilegedIOException
argument_list|(
name|url
operator|::
name|openConnection
argument_list|)
decl_stmt|;
name|urlConnection
operator|.
name|setConnectTimeout
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|in
operator|=
name|SocketAccess
operator|.
name|doPrivilegedIOException
argument_list|(
name|urlConnection
operator|::
name|getInputStream
argument_list|)
expr_stmt|;
name|BufferedReader
name|urlReader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|metadataResult
init|=
name|urlReader
operator|.
name|readLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|metadataResult
operator|==
literal|null
operator|||
name|metadataResult
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"no gce metadata returned from ["
operator|+
name|url
operator|+
literal|"] for ["
operator|+
name|type
operator|.
name|configName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// only one address: because we explicitly ask for only one via the Ec2HostnameType
return|return
operator|new
name|InetAddress
index|[]
block|{
name|InetAddress
operator|.
name|getByName
argument_list|(
name|metadataResult
argument_list|)
block|}
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"IOException caught when fetching InetAddress from ["
operator|+
name|metadataUrl
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|resolveDefault
specifier|public
name|InetAddress
index|[]
name|resolveDefault
parameter_list|()
block|{
return|return
literal|null
return|;
comment|// using this, one has to explicitly specify _ec2_ in network setting
comment|//        return resolve(Ec2HostnameType.DEFAULT, false);
block|}
annotation|@
name|Override
DECL|method|resolveIfPossible
specifier|public
name|InetAddress
index|[]
name|resolveIfPossible
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Ec2HostnameType
name|type
range|:
name|Ec2HostnameType
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|type
operator|.
name|configName
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
name|resolve
argument_list|(
name|type
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

