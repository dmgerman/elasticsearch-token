begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
package|;
end_package

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
name|stream
operator|.
name|StreamInput
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
name|stream
operator|.
name|StreamOutput
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
name|stream
operator|.
name|Writeable
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
name|XContentBuilder
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * SnapshotId - snapshot name + snapshot UUID  */
end_comment

begin_class
DECL|class|SnapshotId
specifier|public
specifier|final
class|class
name|SnapshotId
implements|implements
name|Comparable
argument_list|<
name|SnapshotId
argument_list|>
implements|,
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|NAME
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"name"
decl_stmt|;
DECL|field|UUID
specifier|private
specifier|static
specifier|final
name|String
name|UUID
init|=
literal|"uuid"
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|uuid
specifier|private
specifier|final
name|String
name|uuid
decl_stmt|;
comment|// Caching hash code
DECL|field|hashCode
specifier|private
specifier|final
name|int
name|hashCode
decl_stmt|;
comment|/**      * Constructs a new snapshot      *      * @param name   snapshot name      * @param uuid   snapshot uuid      */
DECL|method|SnapshotId
specifier|public
name|SnapshotId
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|String
name|uuid
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|uuid
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|uuid
argument_list|)
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|computeHashCode
argument_list|()
expr_stmt|;
block|}
comment|/**      * Constructs a new snapshot from a input stream      *      * @param in  input stream      */
DECL|method|SnapshotId
specifier|public
name|SnapshotId
parameter_list|(
specifier|final
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|uuid
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|hashCode
operator|=
name|computeHashCode
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns snapshot name      *      * @return snapshot name      */
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**      * Returns the snapshot UUID      *      * @return snapshot uuid      */
DECL|method|getUUID
specifier|public
name|String
name|getUUID
parameter_list|()
block|{
return|return
name|uuid
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
operator|+
literal|"/"
operator|+
name|uuid
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|SnapshotId
name|that
init|=
operator|(
name|SnapshotId
operator|)
name|o
decl_stmt|;
return|return
name|name
operator|.
name|equals
argument_list|(
name|that
operator|.
name|name
argument_list|)
operator|&&
name|uuid
operator|.
name|equals
argument_list|(
name|that
operator|.
name|uuid
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|SnapshotId
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|name
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|name
argument_list|)
return|;
block|}
DECL|method|computeHashCode
specifier|private
name|int
name|computeHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|name
argument_list|,
name|uuid
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|uuid
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|NAME
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|UUID
argument_list|,
name|uuid
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|SnapshotId
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the new format from 5.0 which contains the snapshot name and uuid
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
name|String
name|uuid
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|NAME
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|name
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|UUID
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|uuid
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|SnapshotId
argument_list|(
name|name
argument_list|,
name|uuid
argument_list|)
return|;
block|}
else|else
block|{
comment|// the old format pre 5.0 that only contains the snapshot name, use the name as the uuid too
specifier|final
name|String
name|name
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
return|return
operator|new
name|SnapshotId
argument_list|(
name|name
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

