/*!
* Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.googledrive.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class GoogleDriveFileSystem extends AbstractFileSystem implements FileSystem {

  public GoogleDriveFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new GoogleDriveFileObject( abstractFileName, this );
  }

  public void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( GoogleDriveFileProvider.capabilities );
  }

  protected void clearFileFromCache( FileName name ) {
    super.removeFileFromCache( name );
  }

  public FileObject resolveFile( FileName name ) throws FileSystemException {
    return this.processFile( name, true );
  }

  private synchronized FileObject processFile( FileName name, boolean useCache ) throws FileSystemException {
    if ( !super.getRootName().getRootURI().equals( name.getRootURI() ) ) {
      throw new FileSystemException( "vfs.provider/mismatched-fs-for-name.error",
          new Object[] { name, super.getRootName(), name.getRootURI() } );
    } else {
      FileObject file;
      if ( useCache ) {
        file = super.getFileFromCache( name );
      } else {
        file = null;
      }

      if ( file == null ) {
        try {
          file = this.createFile( (AbstractFileName) name );
        } catch ( Exception var5 ) {
          return null;
        }

        file = super.decorateFileObject( file );
        if ( useCache ) {
          super.putFileToCache( file );
        }
      }

      if ( super.getFileSystemManager().getCacheStrategy().equals( CacheStrategy.ON_RESOLVE ) ) {
        file.refresh();
      }

      return file;
    }
  }

}
