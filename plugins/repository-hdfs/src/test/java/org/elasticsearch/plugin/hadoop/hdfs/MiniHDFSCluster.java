begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.hadoop.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|hadoop
operator|.
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|FileUtil
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
name|hadoop
operator|.
name|hdfs
operator|.
name|server
operator|.
name|datanode
operator|.
name|DataNode
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
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_class
DECL|class|MiniHDFSCluster
specifier|public
class|class
name|MiniHDFSCluster
block|{
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"Hadoop is messy"
argument_list|)
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
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.data"
argument_list|,
literal|"build/test/data"
argument_list|)
argument_list|,
literal|"dfs/"
argument_list|)
argument_list|)
expr_stmt|;
comment|// MiniHadoopClusterManager.main(new String[] { "-nomr" });
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
name|DataNode
operator|.
name|DATA_DIR_PERMISSION_KEY
argument_list|,
literal|"666"
argument_list|)
expr_stmt|;
name|cfg
operator|.
name|set
argument_list|(
literal|"dfs.replication"
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|dfsCluster
init|=
operator|new
name|MiniDFSCluster
argument_list|(
name|cfg
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|fs
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dfsCluster
operator|.
name|getHftpFileSystem
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|// dfsCluster.shutdown();
block|}
block|}
end_class

end_unit

