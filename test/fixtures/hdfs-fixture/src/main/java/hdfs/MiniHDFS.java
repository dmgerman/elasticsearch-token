begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|hdfs
package|package
name|hdfs
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSConfigKeys
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|MiniDFSCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|BasicConfigurator
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
name|Paths
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
name|StandardCopyOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_comment
comment|/**  * MiniHDFS test fixture. There is a CLI tool, but here we can  * easily properly setup logging, avoid parsing JSON, etc.  */
end_comment

begin_class
DECL|class|MiniHDFS
specifier|public
class|class
name|MiniHDFS
block|{
DECL|field|PORT_FILE_NAME
specifier|private
specifier|static
name|String
name|PORT_FILE_NAME
init|=
literal|"ports"
decl_stmt|;
DECL|field|PID_FILE_NAME
specifier|private
specifier|static
name|String
name|PID_FILE_NAME
init|=
literal|"pid"
decl_stmt|;
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"MiniHDFS<baseDirectory>"
argument_list|)
throw|;
block|}
comment|// configure logging, so we see all HDFS server logs if something goes wrong
name|BasicConfigurator
operator|.
name|configure
argument_list|()
expr_stmt|;
comment|// configure Paths
name|Path
name|baseDir
init|=
name|Paths
operator|.
name|get
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
comment|// hadoop-home/, so logs will not complain
name|Path
name|hadoopHome
init|=
name|baseDir
operator|.
name|resolve
argument_list|(
literal|"hadoop-home"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|hadoopHome
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.home.dir"
argument_list|,
name|hadoopHome
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// hdfs-data/, where any data is going
name|Path
name|hdfsHome
init|=
name|baseDir
operator|.
name|resolve
argument_list|(
literal|"hdfs-data"
argument_list|)
decl_stmt|;
comment|// start cluster
name|Configuration
name|cfg
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|cfg
operator|.
name|set
argument_list|(
name|MiniDFSCluster
operator|.
name|HDFS_MINIDFS_BASEDIR
argument_list|,
name|hdfsHome
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// lower default permission: TODO: needed?
name|cfg
operator|.
name|set
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_DATANODE_DATA_DIR_PERMISSION_KEY
argument_list|,
literal|"766"
argument_list|)
expr_stmt|;
comment|// TODO: remove hardcoded port!
name|MiniDFSCluster
name|dfs
init|=
operator|new
name|MiniDFSCluster
operator|.
name|Builder
argument_list|(
name|cfg
argument_list|)
operator|.
name|nameNodePort
argument_list|(
literal|9999
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// write our PID file
name|Path
name|tmp
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
name|baseDir
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|pid
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|split
argument_list|(
literal|"@"
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|tmp
argument_list|,
name|pid
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|tmp
argument_list|,
name|baseDir
operator|.
name|resolve
argument_list|(
name|PID_FILE_NAME
argument_list|)
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
comment|// write our port file
name|tmp
operator|=
name|Files
operator|.
name|createTempFile
argument_list|(
name|baseDir
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|tmp
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|dfs
operator|.
name|getNameNodePort
argument_list|()
argument_list|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|tmp
argument_list|,
name|baseDir
operator|.
name|resolve
argument_list|(
name|PORT_FILE_NAME
argument_list|)
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
comment|// don't rely on hadoop thread leaks, wait forever, until you kill me
name|Thread
operator|.
name|sleep
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

