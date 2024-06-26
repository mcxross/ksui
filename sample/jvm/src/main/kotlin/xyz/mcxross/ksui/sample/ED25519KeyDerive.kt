 /*
 * Copyright 2022-2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package xyz.mcxross.ksui.sample

 import com.google.common.primitives.Bytes
import org.apache.commons.lang3.StringUtils
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.crypto.HDUtils
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*


 /**
 * Derive ed25519 key from path.
 * 
 * @author fearlessfe
 * @since 2023.02
 */
class ED25519KeyDerive /**
 * Instantiates a new Ed 25519 key derive.
 * 
 * @param key the key
 * @param chaincode the chaincode
 */(
     /**
 * Get key byte [ ].
 * 
 * @return the byte [ ]
 */
     val key: ByteArray, /**
 * Get chaincode byte [ ].
 * 
 * @return the byte [ ]
 */
     val chaincode: ByteArray) {
 /**
 * Derive ed 25519 key derive.
 * 
 * @param index the index
 * @return the ed 25519 key derive
 */
     fun derive(index: Int): ED25519KeyDerive {
        if (!hasHardenedBit(index)) {
      // todo: create an exception
            throw RuntimeException()
        }
        
         val indexBytes = ByteArray(4)
        ByteBuffer.wrap(indexBytes).putInt(index)
        
         val data = Bytes.concat(byteArrayOf(0x00), this.key, indexBytes)
        
         val i = HDUtils.hmacSha512(this.chaincode, data)
         val il = Arrays.copyOfRange(i, 0, 32)
         val ir = Arrays.copyOfRange(i, 32, 64)
        
        return ED25519KeyDerive(il, ir)
    }
    
     /**
 * Derive from path ed 25519 key derive.
 * 
 * @param path the path
 * @return the ed 25519 key derive
 */
     fun deriveFromPath(path: String?): ED25519KeyDerive {
         var path = path
        if (StringUtils.isAnyBlank(path)) {
            path = DEFAULT_DERIVE_PATH
        }
         val hdPath = HDPath.parsePath(path!!)
         val it: Iterator<ChildNumber> = hdPath.iterator()
         var current = this
        while (it.hasNext()) {
            current = current.derive(it.next().i)
        }
        return current
    }
    
    private fun hasHardenedBit(a: Int): Boolean {
        return (a and ChildNumber.HARDENED_BIT) != 0
    }
    
     companion object {
        private const val DEFAULT_DERIVE_PATH = "m/44H/784H/0H/0H/0H"
         /**
 * Create key by default path ed 25519 key derive.
 * 
 * @param seed the seed
 * @return the ed 25519 key derive
 */
         fun createKeyByDefaultPath(seed: ByteArray?): ED25519KeyDerive {
            return createMasterKey(seed).deriveFromPath("")
        }
        
         /**
 * Create master key ed 25519 key derive.
 * 
 * @param seed the seed
 * @return the ed 25519 key derive
 */
         fun createMasterKey(seed: ByteArray?): ED25519KeyDerive {
             val i = HDUtils.hmacSha512("ed25519 seed".toByteArray(Charset.defaultCharset()), seed)
             val il = Arrays.copyOfRange(i, 0, 32)
             val ir = Arrays.copyOfRange(i, 32, 64)
            return ED25519KeyDerive(il, ir)
        }
 }}
