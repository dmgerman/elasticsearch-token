begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.geoip
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|geoip
package|;
end_package

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|DatabaseReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeModule
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
name|nio
operator|.
name|file
operator|.
name|PathMatcher
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
name|StandardOpenOption
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
import|;
end_import

begin_class
DECL|class|IngestGeoIpPlugin
specifier|public
class|class
name|IngestGeoIpPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"ingest-geoip"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Ingest processor that adds information about the geographical location of ip addresses"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|NodeModule
name|nodeModule
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|geoIpConfigDirectory
init|=
name|nodeModule
operator|.
name|getNode
argument_list|()
operator|.
name|getEnvironment
argument_list|()
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"ingest-geoip"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReader
argument_list|>
name|databaseReaders
init|=
name|loadDatabaseReaders
argument_list|(
name|geoIpConfigDirectory
argument_list|)
decl_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|GeoIpProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|templateService
parameter_list|)
lambda|->
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|loadDatabaseReaders
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReader
argument_list|>
name|loadDatabaseReaders
parameter_list|(
name|Path
name|geoIpConfigDirectory
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|geoIpConfigDirectory
argument_list|)
operator|==
literal|false
operator|&&
name|Files
operator|.
name|isDirectory
argument_list|(
name|geoIpConfigDirectory
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"the geoip directory ["
operator|+
name|geoIpConfigDirectory
operator|+
literal|"] containing databases doesn't exist"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReader
argument_list|>
name|databaseReaders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|Stream
argument_list|<
name|Path
argument_list|>
name|databaseFiles
init|=
name|Files
operator|.
name|list
argument_list|(
name|geoIpConfigDirectory
argument_list|)
init|)
block|{
name|PathMatcher
name|pathMatcher
init|=
name|geoIpConfigDirectory
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getPathMatcher
argument_list|(
literal|"glob:**.mmdb"
argument_list|)
decl_stmt|;
comment|// Use iterator instead of forEach otherwise IOException needs to be caught twice...
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iterator
init|=
name|databaseFiles
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Path
name|databasePath
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|isRegularFile
argument_list|(
name|databasePath
argument_list|)
operator|&&
name|pathMatcher
operator|.
name|matches
argument_list|(
name|databasePath
argument_list|)
condition|)
block|{
try|try
init|(
name|InputStream
name|inputStream
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|databasePath
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|)
init|)
block|{
name|databaseReaders
operator|.
name|put
argument_list|(
name|databasePath
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|inputStream
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|databaseReaders
argument_list|)
return|;
block|}
block|}
end_class

end_unit

