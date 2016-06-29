begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|blobstore
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|ParseFieldMatcher
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
name|blobstore
operator|.
name|BlobContainer
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|FromXContentBuilder
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentHelper
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotInfo
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
name|Locale
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
comment|/**  * Base class that handles serialization of various data structures during snapshot/restore operations.  */
end_comment

begin_class
DECL|class|BlobStoreFormat
specifier|public
specifier|abstract
class|class
name|BlobStoreFormat
parameter_list|<
name|T
extends|extends
name|ToXContent
parameter_list|>
block|{
DECL|field|blobNameFormat
specifier|protected
specifier|final
name|String
name|blobNameFormat
decl_stmt|;
DECL|field|reader
specifier|protected
specifier|final
name|FromXContentBuilder
argument_list|<
name|T
argument_list|>
name|reader
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|protected
specifier|final
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
comment|// Serialization parameters to specify correct context for metadata serialization
DECL|field|SNAPSHOT_ONLY_FORMAT_PARAMS
specifier|protected
specifier|static
specifier|final
name|ToXContent
operator|.
name|Params
name|SNAPSHOT_ONLY_FORMAT_PARAMS
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|snapshotOnlyParams
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// when metadata is serialized certain elements of the metadata shouldn't be included into snapshot
comment|// exclusion of these elements is done by setting MetaData.CONTEXT_MODE_PARAM to MetaData.CONTEXT_MODE_SNAPSHOT
name|snapshotOnlyParams
operator|.
name|put
argument_list|(
name|MetaData
operator|.
name|CONTEXT_MODE_PARAM
argument_list|,
name|MetaData
operator|.
name|CONTEXT_MODE_SNAPSHOT
argument_list|)
expr_stmt|;
comment|// serialize SnapshotInfo using the SNAPSHOT mode
name|snapshotOnlyParams
operator|.
name|put
argument_list|(
name|SnapshotInfo
operator|.
name|CONTEXT_MODE_PARAM
argument_list|,
name|SnapshotInfo
operator|.
name|CONTEXT_MODE_SNAPSHOT
argument_list|)
expr_stmt|;
name|SNAPSHOT_ONLY_FORMAT_PARAMS
operator|=
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|snapshotOnlyParams
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param blobNameFormat format of the blobname in {@link String#format(Locale, String, Object...)} format      * @param reader the prototype object that can deserialize objects with type T      * @param parseFieldMatcher parse field matcher      */
DECL|method|BlobStoreFormat
specifier|protected
name|BlobStoreFormat
parameter_list|(
name|String
name|blobNameFormat
parameter_list|,
name|FromXContentBuilder
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|blobNameFormat
operator|=
name|blobNameFormat
expr_stmt|;
name|this
operator|.
name|parseFieldMatcher
operator|=
name|parseFieldMatcher
expr_stmt|;
block|}
comment|/**      * Reads and parses the blob with given blob name.      *      * @param blobContainer blob container      * @param blobName blob name      * @return parsed blob object      */
DECL|method|readBlob
specifier|public
specifier|abstract
name|T
name|readBlob
parameter_list|(
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Reads and parses the blob with given name, applying name translation using the {link #blobName} method      *      * @param blobContainer blob container      * @param name          name to be translated into      * @return parsed blob object      */
DECL|method|read
specifier|public
name|T
name|read
parameter_list|(
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|blobName
init|=
name|blobName
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|readBlob
argument_list|(
name|blobContainer
argument_list|,
name|blobName
argument_list|)
return|;
block|}
comment|/**      * Deletes obj in the blob container      */
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|blobContainer
operator|.
name|deleteBlob
argument_list|(
name|blobName
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks obj in the blob container      */
DECL|method|exists
specifier|public
name|boolean
name|exists
parameter_list|(
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|blobContainer
operator|.
name|blobExists
argument_list|(
name|blobName
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|blobName
specifier|protected
name|String
name|blobName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|blobNameFormat
argument_list|,
name|name
argument_list|)
return|;
block|}
DECL|method|read
specifier|protected
name|T
name|read
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
init|)
block|{
name|T
name|obj
init|=
name|reader
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
return|return
name|obj
return|;
block|}
block|}
block|}
end_class

end_unit

