begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.aws.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
operator|.
name|node
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
name|cloud
operator|.
name|aws
operator|.
name|AwsEc2ServiceImpl
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
name|node
operator|.
name|DiscoveryNodeService
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|Ec2CustomNodeAttributes
specifier|public
class|class
name|Ec2CustomNodeAttributes
extends|extends
name|AbstractComponent
implements|implements
name|DiscoveryNodeService
operator|.
name|CustomAttributesProvider
block|{
DECL|method|Ec2CustomNodeAttributes
specifier|public
name|Ec2CustomNodeAttributes
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
annotation|@
name|Override
DECL|method|buildAttributes
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|buildAttributes
parameter_list|()
block|{
if|if
condition|(
operator|!
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cloud.node.auto_attributes"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ec2Attributes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|URLConnection
name|urlConnection
decl_stmt|;
name|InputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|AwsEc2ServiceImpl
operator|.
name|EC2_METADATA_URL
operator|+
literal|"placement/availability-zone"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"obtaining ec2 [placement/availability-zone] from ec2 meta-data url {}"
argument_list|,
name|url
argument_list|)
expr_stmt|;
name|urlConnection
operator|=
name|url
operator|.
name|openConnection
argument_list|()
expr_stmt|;
name|urlConnection
operator|.
name|setConnectTimeout
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|in
operator|=
name|urlConnection
operator|.
name|getInputStream
argument_list|()
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
name|logger
operator|.
name|error
argument_list|(
literal|"no ec2 metadata returned from {}"
argument_list|,
name|url
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|ec2Attributes
operator|.
name|put
argument_list|(
literal|"aws_availability_zone"
argument_list|,
name|metadataResult
argument_list|)
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
name|debug
argument_list|(
literal|"failed to get metadata for [placement/availability-zone]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
return|return
name|ec2Attributes
return|;
block|}
block|}
end_class

end_unit
