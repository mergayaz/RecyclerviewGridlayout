package kz.kuz.recyclerviewgridlayout

import androidx.recyclerview.widget.RecyclerView
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import android.media.AudioAttributes
import android.content.res.AssetFileDescriptor
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import java.io.IOException
import java.util.ArrayList

class MainFragment : Fragment() {
    private lateinit var mMainRecyclerView: RecyclerView
    private lateinit var mAdapter: MainAdapter
    private lateinit var mSoundPool: SoundPool

    private inner class Sound {
        lateinit var soundName: String
        var soundID = 0
    }

    private val sounds: MutableList<Sound> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true // чтобы фрагмент не уничтожался с активностью
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mMainRecyclerView = view.findViewById(R.id.main_recycler_view)
        mMainRecyclerView.layoutManager = GridLayoutManager(activity, 3)
        // layoutManager можно определить в самом XML
        val mAssets = context?.assets
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        mSoundPool = SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(5)
                .build()
        var afd: AssetFileDescriptor
        var mSound: Int
        try {
            val filenames = mAssets?.list("my_sounds")
            for (filename in filenames!!) {
                val sound = Sound()
                sound.soundName = filename.replace(".wav", "")
                afd = mAssets.openFd("my_sounds/$filename")
                mSound = mSoundPool.load(afd, 1)
                sound.soundID = mSound
                sounds.add(sound)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mAdapter = MainAdapter(sounds)
        mMainRecyclerView.adapter = mAdapter
        return view
    }

    private inner class MainAdapter(private val mSounds: List<Sound>) :
            RecyclerView.Adapter<MainAdapter.MainHolder>() {

        private inner class MainHolder(inflater: LayoutInflater, parent: ViewGroup?) :
                RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item, parent,
                        false)) {
            val mButton: Button = itemView.findViewById(R.id.button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return MainHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: MainHolder, position: Int) {
            val sound = mSounds[position]
            holder.mButton.text = sound.soundName
            holder.mButton.setOnClickListener {
                // нужно быть внимательным, иногда слушатель здесь работает неточно, тогда его лучше
                // реализовать в класса MainHolder
                mSoundPool.play(sound.soundID, 1.0f, 1.0f, 1, 0,
                        1.0f)
            }
        }

        override fun getItemCount(): Int {
            return mSounds.size
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSoundPool.release() // освобождение ресурсов SoundPool
    }

    override fun onResume() {
        super.onResume()
        mAdapter.notifyDataSetChanged() // обновление в случае изменений в списке
    }
}